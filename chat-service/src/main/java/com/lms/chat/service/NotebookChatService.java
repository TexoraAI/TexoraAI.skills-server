package com.lms.chat.service;

import com.lms.chat.entity.Notebook;
import com.lms.chat.entity.NotebookChatMessage;
import com.lms.chat.entity.NotebookSource;
import com.lms.chat.repository.NotebookChatMessageRepository;
import com.lms.chat.repository.NotebookRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotebookChatService {

    // Total characters of extracted source content allowed across ALL sources
    // combined, so a handful of long PDFs/videos can't blow past the model's
    // context window. Oldest sources get truncated/dropped first when over budget.
    private static final int SOURCE_CONTENT_CHAR_BUDGET = 12000;

    private final NotebookRepository notebookRepository;
    private final OpenAiService openAiService;
    private final NotebookUsageService notebookUsageService;
    private final NotebookChatMessageRepository notebookChatMessageRepository;

    public NotebookChatService(NotebookRepository notebookRepository,
                               OpenAiService openAiService,
                               NotebookUsageService notebookUsageService,
                               NotebookChatMessageRepository notebookChatMessageRepository) {
        this.notebookRepository = notebookRepository;
        this.openAiService = openAiService;
        this.notebookUsageService = notebookUsageService;
        this.notebookChatMessageRepository = notebookChatMessageRepository;
    }

    public String chat(Long notebookId, String studentEmail, String userMessage, String organizationId) {
        notebookUsageService.checkAndIncrement(studentEmail, organizationId);

        // 1. Fetch the notebook with its sources
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        // 2. Persist the student's message before calling OpenAI, so it's saved
        // even if the AI call fails downstream.
        saveMessage(nb, NotebookChatMessage.Role.USER, userMessage);

        // 3. Build context from sources
        String sourcesContext = buildSourcesContext(nb);

        // 4. Build system prompt
        String systemPrompt = """
                You are an AI study assistant for a student's notebook titled "%s".
                
                The student has added the following sources to this notebook:
                %s
                
                Your job is to:
                - Answer questions based on the notebook's sources and topic
                - Help the student understand concepts
                - Generate study materials like summaries, flashcards, quizzes when asked
                - Be concise, clear, and educational
                
                If you don't have enough information from the sources, say so honestly.
                """.formatted(nb.getTitle(), sourcesContext);

        // 5. Call OpenAI
        String reply = openAiService.chat(systemPrompt, userMessage);

        // 6. Persist the AI's reply
        saveMessage(nb, NotebookChatMessage.Role.AI, reply);

        return reply;
    }

    /**
     * Returns the full ordered chat history for a notebook, after verifying
     * the notebook belongs to studentEmail (same ownership check pattern used
     * by chat() / generateAutoOverview()).
     */
    public List<NotebookChatMessage> getHistory(Long notebookId, String studentEmail) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        return notebookChatMessageRepository.findByNotebook_IdOrderByCreatedAtAsc(nb.getId());
    }

    private void saveMessage(Notebook nb, NotebookChatMessage.Role role, String content) {
        NotebookChatMessage message = new NotebookChatMessage();
        message.setNotebook(nb);
        message.setRole(role);
        message.setContent(content);
        notebookChatMessageRepository.save(message);
    }

    /**
     * Generates a NotebookLM-style auto overview of a notebook's source content.
     * Intended to be triggered once, right after the notebook's FIRST source is
     * added, so the student gets an onboarding-style summary without having to
     * type anything. Reuses buildSourcesContext(nb) for grounding, exactly like
     * chat() does, and applies the same usage-limit check so this can't be used
     * to bypass the notebook's usage limits.
     */
    public String generateAutoOverview(Long notebookId, String studentEmail, String organizationId) {
        notebookUsageService.checkAndIncrement(studentEmail, organizationId);

        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        String sourcesContext = buildSourcesContext(nb);

        String systemPrompt = """
                You are an AI study assistant preparing a welcoming introduction for a
                student's notebook titled "%s".
                
                The notebook contains the following source content:
                %s
                
                Write a natural, well-organized overview of this source in 2-4 short
                paragraphs, similar in tone to the introduction of a study guide. Help
                the student quickly understand what the source covers and why it's
                useful. Do not ask the student any questions and do not request any
                further input — this overview is shown automatically, before the
                student has typed anything.
                """.formatted(nb.getTitle(), sourcesContext);

        return openAiService.chat(systemPrompt, "Generate the overview now.");
    }

    /**
     * Builds the sources section of the system prompt, including each source's
     * extracted content (not just title/type/url), capped to a combined
     * character budget across all sources. When the budget is exceeded, the
     * least-recently-added sources are truncated (or omitted) first, since the
     * most recently added sources are more likely to be what the student is
     * currently asking about.
     */
    private String buildSourcesContext(Notebook nb) {
        List<NotebookSource> sources = nb.getSources();

        if (sources == null || sources.isEmpty()) {
            return "No sources added yet. Answer based on the notebook topic: "
                   + nb.getTitle();
        }

        // Newest first: budget gets spent on the most recently added sources,
        // so anything left over runs out on the oldest ones first.
        List<NotebookSource> newestFirst = sources.stream()
                .sorted(Comparator.comparing(
                        NotebookSource::getCreatedAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ).reversed())
                .collect(Collectors.toList());

        int remainingBudget = SOURCE_CONTENT_CHAR_BUDGET;
        boolean truncatedAny = false;
        List<String> blocks = new ArrayList<>();

        for (NotebookSource src : newestFirst) {
            StringBuilder block = new StringBuilder();
            block.append("- [").append(src.getSourceType()).append("] ").append(src.getTitle());
            if (src.getUrl() != null) {
                block.append(" (").append(src.getUrl()).append(")");
            }

            String content = src.getExtractedContent();
            if (content != null && !content.isBlank()) {
                if (remainingBudget <= 0) {
                    block.append("\n  [content omitted to stay within context budget]");
                    truncatedAny = true;
                } else if (content.length() > remainingBudget) {
                    block.append("\n  Content: ").append(content, 0, remainingBudget).append("...");
                    truncatedAny = true;
                    remainingBudget = 0;
                } else {
                    block.append("\n  Content: ").append(content);
                    remainingBudget -= content.length();
                }
            }

            blocks.add(block.toString());
        }

        // Put back in chronological order for a more natural-reading prompt.
        Collections.reverse(blocks);

        String context = String.join("\n\n", blocks);
        if (truncatedAny) {
            context += "\n\n[Note: some source content above was truncated or omitted to fit the "
                     + "assistant's context budget. Older sources were truncated first; if the "
                     + "student's question depends on truncated content, say so honestly.]";
        }
        return context;
    }

}