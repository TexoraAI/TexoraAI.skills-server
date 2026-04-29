package com.lms.chat.service;

import com.lms.chat.entity.Notebook;
import com.lms.chat.entity.NotebookSource;
import com.lms.chat.repository.NotebookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotebookChatService {

    private final NotebookRepository notebookRepository;
    private final OpenAiService openAiService;

    public NotebookChatService(NotebookRepository notebookRepository,
                               OpenAiService openAiService) {
        this.notebookRepository = notebookRepository;
        this.openAiService = openAiService;
    }

    public String chat(Long notebookId, String studentEmail, String userMessage) {
        // 1. Fetch the notebook with its sources
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        // 2. Build context from sources
        String sourcesContext = buildSourcesContext(nb);

        // 3. Build system prompt
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

        // 4. Call OpenAI
        return openAiService.chat(systemPrompt, userMessage);
    }

    private String buildSourcesContext(Notebook nb) {
        List<NotebookSource> sources = nb.getSources();
        
        if (sources == null || sources.isEmpty()) {
            return "No sources added yet. Answer based on the notebook topic: " 
                   + nb.getTitle();
        }

        return sources.stream()
                .map(src -> "- [" + src.getSourceType() + "] " 
                            + src.getTitle() 
                            + (src.getUrl() != null ? " (" + src.getUrl() + ")" : ""))
                .collect(Collectors.joining("\n"));
    }
    
}