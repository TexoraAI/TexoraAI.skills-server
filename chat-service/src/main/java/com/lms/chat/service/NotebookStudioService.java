package com.lms.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.chat.dto.NotebookStudioOutputResponse;
import com.lms.chat.entity.Notebook;
import com.lms.chat.entity.NotebookSource;
import com.lms.chat.entity.NotebookStudioOutput;
import com.lms.chat.repository.NotebookRepository;
import com.lms.chat.repository.NotebookStudioOutputRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Central orchestrator for all 9 "Studio" output types. Grounds text-based
 * types directly in the notebook's real source content, forces strict JSON
 * for structured types (with one retry on parse failure), and delegates
 * audio/slides/video/infographic to their dedicated services (or, for
 * infographic, to OpenAI's image generation endpoint directly). Also exposes
 * generateTopicSuggestions(...) (content-aware suggestion chips, shared by
 * Audio/Video/Flashcards/Quiz) and generateReportFormatSuggestions(...)
 * (content-aware report-format picker for Reports).
 *
 * Generation is asynchronous: createPendingOutput(...) does the fast
 * synchronous work (ownership/usage-limit checks, creating a PENDING row)
 * and returns immediately, while processGenerationAsync(...) does the slow
 * work (the actual AI/media generation) on a dedicated background thread
 * pool and updates the row to READY or FAILED when done. The frontend polls
 * GET /notebooks/{id}/studio to observe the transition.
 */
@Service
public class NotebookStudioService {

    // Same combined-source character budget approach as NotebookChatService.
    private static final int SOURCE_CONTENT_CHAR_BUDGET = 12000;

    // Reports' 3 fixed built-in formats. Anything else (AI-suggested titles,
    // "Create Your Own") is treated as a free-form format title driven by
    // its description.
    private static final String REPORT_FORMAT_BRIEFING_DOC = "Briefing Doc";
    private static final String REPORT_FORMAT_STUDY_GUIDE = "Study Guide";
    private static final String REPORT_FORMAT_BLOG_POST = "Blog Post";
    private static final String REPORT_FORMAT_CREATE_YOUR_OWN = "Create Your Own";

    private final NotebookRepository notebookRepository;
    private final NotebookStudioOutputRepository studioOutputRepository;
    private final OpenAiService openAiService;
    private final NotebookUsageService notebookUsageService;
    private final NotebookAudioService notebookAudioService;
    private final NotebookSlideService notebookSlideService;
    private final NotebookVideoService notebookVideoService;
    private final S3Service s3Service;
    private final ObjectMapper objectMapper;

    // Self-reference obtained through Spring's proxy, injected lazily to avoid
    // a circular-construction issue. This is required so that the call to
    // processGenerationAsync(...) from within createPendingOutput(...) goes
    // through the Spring AOP proxy — a plain "this.processGenerationAsync(...)"
    // self-invocation would silently bypass both @Async and @Transactional,
    // since Spring's proxy-based AOP only intercepts calls that come in from
    // outside the bean.
    private final NotebookStudioService self;

    public NotebookStudioService(NotebookRepository notebookRepository,
                                  NotebookStudioOutputRepository studioOutputRepository,
                                  OpenAiService openAiService,
                                  NotebookUsageService notebookUsageService,
                                  NotebookAudioService notebookAudioService,
                                  NotebookSlideService notebookSlideService,
                                  NotebookVideoService notebookVideoService,
                                  S3Service s3Service,
                                  ObjectMapper objectMapper,
                                  @Lazy NotebookStudioService self) {
        this.notebookRepository = notebookRepository;
        this.studioOutputRepository = studioOutputRepository;
        this.openAiService = openAiService;
        this.notebookUsageService = notebookUsageService;
        this.notebookAudioService = notebookAudioService;
        this.notebookSlideService = notebookSlideService;
        this.notebookVideoService = notebookVideoService;
        this.s3Service = s3Service;
        this.objectMapper = objectMapper;
        this.self = self;
    }

    // ── ASYNC GENERATION ENTRY POINTS ────────────────────────────────

    /**
     * Fast synchronous half of studio generation. Enforces ownership and the
     * usage limit (so limits can't be bypassed by queuing many requests in
     * the background), creates a PENDING NotebookStudioOutput row, kicks off
     * the slow work in the background, and returns immediately.
     */
    @Transactional
    public NotebookStudioOutputResponse createPendingOutput(Long notebookId, String type, String language,
                                                              String studentEmail, String organizationId,
                                                              String style, String aspectRatio,
                                                              String levelOfDetail, String guidance,
                                                              String format, String length, String focus,
                                                              String visualStyle, List<Long> sourceIds,
                                                              String formatTitle, String description,
                                                              String topic, String cardCountLevel,
                                                              String questionCountLevel, String difficulty) {
        notebookUsageService.checkAndIncrement(studentEmail, organizationId);

        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        NotebookStudioOutput.StudioType studioType;
        try {
            studioType = NotebookStudioOutput.StudioType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Unknown studio type: " + type);
        }

        NotebookStudioOutput output = new NotebookStudioOutput();
        output.setNotebook(nb);
        output.setType(studioType);
        output.setLanguage(language);
        output.setStatus(NotebookStudioOutput.Status.PENDING);

        NotebookStudioOutput saved = studioOutputRepository.save(output);

        // Only plain/primitive params cross into the background thread — never
        // the Notebook or NotebookStudioOutput entities, since they're bound to
        // this request's persistence context and aren't safe to use from a new
        // thread/transaction.
        try {
            self.processGenerationAsync(saved.getId(), notebookId, type, language,
                    style, aspectRatio, levelOfDetail, guidance,
                    format, length, focus, visualStyle, sourceIds,
                    formatTitle, description, topic, cardCountLevel, questionCountLevel, difficulty);
        } catch (TaskRejectedException e) {
            // All 4 threads busy and the queue (capacity 20) is full — the
            // submission itself was rejected synchronously, right here, before
            // any background work started. Don't lose the request silently.
            saved.setStatus(NotebookStudioOutput.Status.FAILED);
            saved.setErrorMessage("Too many generations in progress, please try again shortly.");
            saved = studioOutputRepository.save(saved);
        }

        return toResponse(saved);
    }

    /**
     * Slow, background half of studio generation. Runs on the dedicated
     * studioGenerationExecutor pool. Re-fetches the Notebook fresh (needed
     * since this runs in a new thread/transaction), runs the exact same
     * per-type generation logic as before, then updates the
     * NotebookStudioOutput row to READY (with textContent/s3Key) or FAILED
     * (with a safe error message) — never leaving it stuck at PENDING.
     */
    @Async("studioGenerationExecutor")
    @Transactional
    public void processGenerationAsync(Long outputId, Long notebookId, String type, String language,
                                        String style, String aspectRatio,
                                        String levelOfDetail, String guidance,
                                        String format, String length, String focus,
                                        String visualStyle, List<Long> sourceIds,
                                        String formatTitle, String description,
                                        String topic, String cardCountLevel,
                                        String questionCountLevel, String difficulty) {
        NotebookStudioOutput.StudioType studioType;
        try {
            studioType = NotebookStudioOutput.StudioType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            markFailed(outputId, "Unknown studio type: " + type);
            return;
        }

        Notebook nb;
        try {
            nb = notebookRepository.findById(notebookId)
                    .orElseThrow(() -> new RuntimeException("Notebook not found"));
        } catch (Exception e) {
            markFailed(outputId, "Notebook could not be found for this generation.");
            return;
        }

        String textContent = null;
        String s3Key = null;

        try {
            switch (studioType) {
                case REPORT -> textContent = generateReport(nb, language, formatTitle, description, sourceIds);
                case DATATABLE -> textContent = generateDataTable(nb, language, description, sourceIds);
                case MINDMAP -> textContent = generateMindmap(nb, language, topic, sourceIds);
                case FLASHCARDS -> textContent =
                        generateFlashcards(nb, language, cardCountLevel, difficulty, topic, sourceIds);
                case QUIZ -> textContent =
                        generateQuiz(nb, language, questionCountLevel, difficulty, topic, sourceIds);
                case AUDIO -> {
                    String script = generateAudioScript(nb, language, format, length, focus, sourceIds);
                    s3Key = notebookAudioService.generate(script, notebookId);
                    textContent = script;
                }
                case SLIDES -> {
                    JsonNode slidesJson = generateSlideDeckJson(nb, language, format, length, description, sourceIds);
                    s3Key = notebookSlideService.generate(slidesJson, notebookId);
                    textContent = slidesJson.toString();
                }
                case VIDEO -> {
                    JsonNode slidesJson = generateSlideJson(nb, language, sourceIds);
                    s3Key = notebookVideoService.generate(slidesJson, notebookId, format, visualStyle, focus);
                    textContent = slidesJson.toString();
                }
                case INFOGRAPHIC -> {
                    InfographicResult result = generateInfographic(
                            nb, notebookId, language, style, aspectRatio, levelOfDetail, guidance);
                    textContent = result.imagePrompt();
                    s3Key = result.s3Key();
                }
            }
        } catch (Exception e) {
            markFailed(outputId, safeErrorMessage(e));
            return;
        }

        markReady(outputId, textContent, s3Key);
    }

    private void markReady(Long outputId, String textContent, String s3Key) {
        studioOutputRepository.findById(outputId).ifPresentOrElse(output -> {
            output.setTextContent(textContent);
            output.setS3Key(s3Key);
            output.setStatus(NotebookStudioOutput.Status.READY);
            output.setErrorMessage(null);
            studioOutputRepository.save(output);
        }, () -> System.err.println(
                "Studio output " + outputId + " was deleted before generation finished; discarding result."));
    }

    private void markFailed(Long outputId, String errorMessage) {
        studioOutputRepository.findById(outputId).ifPresentOrElse(output -> {
            output.setStatus(NotebookStudioOutput.Status.FAILED);
            output.setErrorMessage(errorMessage);
            studioOutputRepository.save(output);
        }, () -> System.err.println(
                "Studio output " + outputId + " was deleted before generation failed; discarding failure."));
    }

    /**
     * The generateX(...) methods already throw clean, human-readable
     * RuntimeExceptions (e.g. "Quiz generation failed: ..."). This just
     * guards against a null/blank message and never surfaces a raw stack
     * trace to the user.
     */
    private String safeErrorMessage(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return "Generation failed due to an unexpected error. Please try again.";
        }
        return message;
    }

    @Transactional(readOnly = true)
    public List<NotebookStudioOutputResponse> listOutputs(Long notebookId, String studentEmail) {
        // Ownership check before exposing any outputs for this notebook
        notebookRepository.findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));

        return studioOutputRepository.findByNotebook_IdOrderByCreatedAtDesc(notebookId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteOutput(Long outputId, String studentEmail) {
        NotebookStudioOutput output = studioOutputRepository
                .findByIdAndNotebook_StudentEmail(outputId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Studio output not found"));

        if (output.getS3Key() != null) {
            try {
                s3Service.deleteFile(output.getS3Key());
            } catch (Exception e) {
                // Don't block DB cleanup just because S3 cleanup failed
                System.err.println("Could not delete S3 object for studio output " + outputId + ": " + e.getMessage());
            }
        }
        studioOutputRepository.delete(output);
    }

    private NotebookStudioOutputResponse toResponse(NotebookStudioOutput output) {
        String downloadUrl = null;
        if (output.getS3Key() != null) {
            try {
                downloadUrl = s3Service.generatePresignedUrl(output.getS3Key(), Duration.ofHours(1));
            } catch (Exception e) {
                System.err.println("Could not generate presigned URL for studio output "
                        + output.getId() + ": " + e.getMessage());
            }
        }
        return NotebookStudioOutputResponse.from(output, downloadUrl);
    }

    // ── PER-TYPE PROMPTS ────────────────────────────────────────────

    /**
     * Two-step flow, step one: asks for exactly 4 content-aware report
     * format suggestions genuinely relevant to this notebook's source
     * material (e.g. "Technical Manual", "Design Framework" for a C
     * programming syllabus), as { title, description } pairs. Powers the
     * Reports format picker alongside the 3 fixed built-in formats and
     * "Create Your Own".
     */
    public JsonNode generateReportFormatSuggestions(Notebook nb) {
        String context = buildSourcesContext(nb);
        String systemPrompt = """
                You suggest report format options for a notebook studio customization
                modal. The notebook is titled "%s".

                Sources:
                %s

                Suggest exactly 4 report formats genuinely well-suited to this specific
                source material — not generic filler. For example, for a C programming
                syllabus you might suggest "Technical Manual" or "Design Framework"; for
                a set of scientific papers you might suggest "Literature Review" or
                "Methodology Breakdown". Tailor the 4 suggestions to what these sources
                actually are.

                Respond with ONLY raw JSON (no markdown fences, no commentary): an array
                of exactly 4 objects matching EXACTLY this shape:
                [
                  { "title": "Format name (a few words)", "description": "One sentence describing this format." }
                ]
                """.formatted(nb.getTitle(), context);

        JsonNode json = callOpenAiForStrictJson(systemPrompt, "Generate the report format suggestions now.");
        if (!json.isArray() || json.isEmpty() || !json.get(0).has("title") || !json.get(0).has("description")) {
            throw new RuntimeException(
                    "Report format suggestion generation failed: AI response did not match the expected [{title, description}] shape");
        }
        return json;
    }

    /**
     * Ownership-checked entry point for the report-format-suggestions
     * endpoint.
     */
    @Transactional(readOnly = true)
    public JsonNode generateReportFormatSuggestions(Long notebookId, String studentEmail) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));
        return generateReportFormatSuggestions(nb);
    }

    /**
     * Step two of Reports: generates the actual report. formatTitle is a
     * free string — either one of the 3 fixed built-in formats ("Briefing
     * Doc" / "Study Guide" / "Blog Post"), "Create Your Own", or any of the
     * 4 AI-suggested titles from generateReportFormatSuggestions(...).
     * description guides structure/focus: optional extra guidance for the
     * fixed formats, but the primary driver of structure for "Create Your
     * Own" and suggested formats (which have no baseline structure of
     * their own).
     */
    private String generateReport(Notebook nb, String language, String formatTitle, String description,
                                   List<Long> sourceIds) {
        String context = buildSourcesContext(nb, sourceIds);
        String resolvedFormatTitle = (formatTitle == null || formatTitle.isBlank())
                ? REPORT_FORMAT_BRIEFING_DOC : formatTitle.trim();
        String resolvedDescription = (description == null) ? "" : description.trim();

        boolean isFixedFormat = REPORT_FORMAT_BRIEFING_DOC.equals(resolvedFormatTitle)
                || REPORT_FORMAT_STUDY_GUIDE.equals(resolvedFormatTitle)
                || REPORT_FORMAT_BLOG_POST.equals(resolvedFormatTitle);

        String formatInstruction = switch (resolvedFormatTitle) {
            case REPORT_FORMAT_BRIEFING_DOC -> "Write this as a BRIEFING DOC: a concise overview surfacing "
                    + "the key insights from the sources, illustrated with short supporting quotes or "
                    + "specifics where useful.";
            case REPORT_FORMAT_STUDY_GUIDE -> "Write this as a STUDY GUIDE: include a short-answer quiz "
                    + "section, a set of suggested essay questions, and a glossary of key terms — all "
                    + "grounded in the sources.";
            case REPORT_FORMAT_BLOG_POST -> "Write this as a BLOG POST: an insightful, highly readable "
                    + "article that draws out the most interesting takeaways from the sources for a "
                    + "general audience.";
            case REPORT_FORMAT_CREATE_YOUR_OWN -> "This is a fully custom report with no fixed structure "
                    + "to follow — structure and write it entirely based on the description below.";
            default -> "Write this as a \"" + resolvedFormatTitle + "\" style report. This format has no "
                    + "fixed baseline structure, so follow the description below closely to determine "
                    + "how it should be structured and what it should focus on.";
        };

        String descriptionInstruction;
        if (!resolvedDescription.isEmpty()) {
            descriptionInstruction = isFixedFormat
                    ? "Additional guidance to incorporate: " + resolvedDescription
                    : "This description is the primary driver of the report's structure and focus — "
                            + "follow it closely: " + resolvedDescription;
        } else if (!isFixedFormat) {
            descriptionInstruction = "No additional guidance was given beyond the format name/description "
                    + "above — use your best judgment for a report in this format, staying grounded in "
                    + "the sources.";
        } else {
            descriptionInstruction = "";
        }

        String systemPrompt = """
                You are an AI study assistant generating a study report for a notebook titled "%s".
                Write the report in %s.
                Base the report strictly on the sources below; if the sources don't cover something, say so honestly rather than inventing facts.

                Sources:
                %s

                %s

                %s

                Write a clear, well-structured report in Markdown with headings and short paragraphs.
                """.formatted(nb.getTitle(), language, context, formatInstruction, descriptionInstruction);
        return openAiService.chat(systemPrompt, "Generate the report now.");
    }

    private String generateDataTable(Notebook nb, String language, String description, List<Long> sourceIds) {
        String context = buildSourcesContext(nb, sourceIds);
        String resolvedDescription = (description == null || description.isBlank())
                ? "Organize whatever key facts, comparisons, or data points best fit the source material — "
                        + "use your best judgment for the columns and grouping."
                : description.trim();

        String systemPrompt = """
                You are an AI study assistant generating a comparison/summary data table for a notebook titled "%s".
                Write all content in %s.
                Base the table strictly on the sources below; if the sources don't cover something, say so honestly rather than inventing facts.

                Sources:
                %s

                Table guidance: %s

                Respond with a single Markdown table (header row + rows) that best organizes the key facts, comparisons, or data points from the sources, following the guidance above. No text before or after the table.
                """.formatted(nb.getTitle(), language, context, resolvedDescription);
        return openAiService.chat(systemPrompt, "Generate the data table now.");
    }

    /**
     * @param topic optional free text; when blank the mind map covers the
     *              whole source material (unchanged prior behavior), when
     *              provided it scopes the map specifically to that topic.
     */
    private String generateMindmap(Notebook nb, String language, String topic, List<Long> sourceIds) {
        String context = buildSourcesContext(nb, sourceIds);
        String resolvedTopic = (topic == null) ? "" : topic.trim();
        String scopeInstruction = resolvedTopic.isEmpty()
                ? "Cover the whole source material."
                : "Scope the mind map specifically to this topic: " + resolvedTopic + " (you may draw on "
                        + "the wider sources for context, but keep the map centered on this topic).";

        String systemPrompt = """
                You are an AI study assistant generating a mind map for a notebook titled "%s", in %s.
                Base it strictly on the sources below. %s

                Sources:
                %s

                Respond with ONLY raw JSON (no markdown fences, no commentary) matching EXACTLY this recursive shape:
                {
                  "root": "Central topic as a short string",
                  "children": [
                    {
                      "label": "Subtopic label",
                      "children": [
                        { "label": "Nested detail", "children": [] }
                      ]
                    }
                  ]
                }
                "children" arrays may be empty but must always be present. Keep labels short (a few words).
                """.formatted(nb.getTitle(), language, scopeInstruction, context);

        JsonNode json = callOpenAiForStrictJson(systemPrompt, "Generate the mind map now.");
        if (!json.has("root") || !json.has("children") || !json.get("children").isArray()) {
            throw new RuntimeException("Mindmap generation failed: AI response did not match the expected {root, children[]} shape");
        }
        return json.toString();
    }

    /**
     * @param cardCountLevel "fewer" (5-7) | "standard" (8-12, default) | "more" (13-18)
     * @param difficulty     "easy" | "medium" (default) | "hard"
     * @param topic          optional free-text scoping; blank covers the whole source material
     */
    private String generateFlashcards(Notebook nb, String language, String cardCountLevel, String difficulty,
                                       String topic, List<Long> sourceIds) {
        String context = buildSourcesContext(nb, sourceIds);
        String resolvedCardCountLevel = normalizeCountLevel(cardCountLevel);
        String resolvedDifficulty = normalizeDifficulty(difficulty);
        String resolvedTopic = (topic == null) ? "" : topic.trim();

        String countInstruction = switch (resolvedCardCountLevel) {
            case "fewer" -> "Generate between 5 and 7 flashcards.";
            case "more" -> "Generate between 13 and 18 flashcards.";
            default -> "Generate between 8 and 12 flashcards.";
        };
        String difficultyInstruction = switch (resolvedDifficulty) {
            case "easy" -> "Keep the questions straightforward, testing basic recall and definitions.";
            case "hard" -> "Make the questions challenging, testing deeper understanding and application "
                    + "rather than simple recall.";
            default -> "Aim for a moderate difficulty, mixing recall with some deeper understanding.";
        };
        String topicInstruction = resolvedTopic.isEmpty()
                ? ""
                : "Scope the flashcards specifically to this topic: " + resolvedTopic + ".";

        String systemPrompt = """
                You are an AI study assistant generating flashcards for a notebook titled "%s", in %s.
                Base them strictly on the sources below.

                Sources:
                %s

                %s

                %s

                %s

                Respond with ONLY raw JSON (no markdown fences, no commentary): an array of objects matching EXACTLY this shape:
                [
                  { "front": "Question or term", "back": "Answer or definition" }
                ]
                """.formatted(nb.getTitle(), language, context, countInstruction, difficultyInstruction, topicInstruction);

        JsonNode json = callOpenAiForStrictJson(systemPrompt, "Generate the flashcards now.");
        if (!json.isArray() || json.isEmpty() || !json.get(0).has("front") || !json.get(0).has("back")) {
            throw new RuntimeException("Flashcard generation failed: AI response did not match the expected [{front, back}] shape");
        }
        return json.toString();
    }

    /**
     * @param questionCountLevel "fewer" (3-4) | "standard" (5-7, default) | "more" (8-12)
     * @param difficulty         "easy" | "medium" (default) | "hard"
     * @param topic              optional free-text scoping; blank covers the whole source material
     */
    private String generateQuiz(Notebook nb, String language, String questionCountLevel, String difficulty,
                                 String topic, List<Long> sourceIds) {
        String context = buildSourcesContext(nb, sourceIds);
        String resolvedQuestionCountLevel = normalizeCountLevel(questionCountLevel);
        String resolvedDifficulty = normalizeDifficulty(difficulty);
        String resolvedTopic = (topic == null) ? "" : topic.trim();

        String countInstruction = switch (resolvedQuestionCountLevel) {
            case "fewer" -> "Generate between 3 and 4 questions.";
            case "more" -> "Generate between 8 and 12 questions.";
            default -> "Generate between 5 and 7 questions.";
        };
        String difficultyInstruction = switch (resolvedDifficulty) {
            case "easy" -> "Keep the questions straightforward, testing basic recall and understanding.";
            case "hard" -> "Make the questions challenging, testing deeper understanding, application, "
                    + "and nuance rather than simple recall.";
            default -> "Aim for a moderate difficulty, mixing recall with some deeper understanding.";
        };
        String topicInstruction = resolvedTopic.isEmpty()
                ? ""
                : "Scope the quiz specifically to this topic: " + resolvedTopic + ".";

        String systemPrompt = """
                You are an AI study assistant generating a multiple-choice quiz for a notebook titled "%s", in %s.
                Base it strictly on the sources below.

                Sources:
                %s

                %s

                %s

                %s

                Respond with ONLY raw JSON (no markdown fences, no commentary): an array of objects matching EXACTLY this shape:
                [
                  {
                    "question": "Question text",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correctIndex": 0,
                    "explanation": "Why the correct answer is correct"
                  }
                ]
                "correctIndex" is a zero-based index into "options".
                """.formatted(nb.getTitle(), language, context, countInstruction, difficultyInstruction, topicInstruction);

        JsonNode json = callOpenAiForStrictJson(systemPrompt, "Generate the quiz now.");
        if (!json.isArray() || json.isEmpty()
                || !json.get(0).has("question") || !json.get(0).has("options") || !json.get(0).has("correctIndex")) {
            throw new RuntimeException("Quiz generation failed: AI response did not match the expected quiz shape");
        }
        return json.toString();
    }

    /**
     * Builds the podcast script prompt for the AUDIO studio type, incorporating
     * the requested format (deepdive/brief/critique/debate), length
     * (short/default/long), an optional free-text focus instruction, and an
     * optional subset of source IDs to ground the generation on.
     */
    private String generateAudioScript(Notebook nb, String language, String format, String length,
                                        String focus, List<Long> sourceIds) {
        String context = buildSourcesContext(nb, sourceIds);

        String resolvedFormat = normalizeAudioFormat(format);
        String resolvedLength = normalizeAudioLength(length);

        String formatInstruction = switch (resolvedFormat) {
            case "brief" -> "Format: BRIEF. Keep this bite-sized: a short, punchy two-host overview "
                    + "that hits only the core ideas and skips tangents or deep unpacking. It should "
                    + "read as noticeably more compact than a full deep-dive discussion.";
            case "critique" -> "Format: CRITIQUE. Frame this as an expert review: HOST_A and HOST_B "
                    + "critically evaluate the source material, discussing its strengths, weaknesses, "
                    + "gaps, and unanswered questions, and offer constructive feedback on how it could "
                    + "be improved or what a reader should watch out for. Keep it as a natural "
                    + "back-and-forth dialogue, not a monologue.";
            case "debate" -> "Format: DEBATE. Frame this as a debate: HOST_A and HOST_B take different, "
                    + "clearly opposing positions on a key topic or claim from the sources and argue "
                    + "their sides persuasively, pushing back on each other's points, while staying "
                    + "grounded in the source material. Let genuine disagreement come through in the "
                    + "dialogue.";
            default -> "Format: DEEP DIVE. HOST_A and HOST_B have a natural, lively conversation that "
                    + "unpacks the material in depth, connecting ideas and topics across the sources "
                    + "and exploring their implications.";
        };

        String lengthInstruction = switch (resolvedLength) {
            case "short" -> "Length: SHORT. Keep the script noticeably shorter than usual — aim for "
                    + "roughly 6-10 total exchanges.";
            case "long" -> "Length: LONG. Make the script longer and more detailed than usual — aim "
                    + "for roughly 30-40 total exchanges, going deeper into more points from the "
                    + "sources.";
            default -> "Length: DEFAULT. Aim for roughly 15-25 total exchanges.";
        };

        String focusInstruction = (focus != null && !focus.isBlank())
                ? "Focus specifically on: " + focus.trim() + ". Keep the hosts' conversation centered "
                        + "on this even as they draw on the wider source material for context."
                : "";

        String systemPrompt = """
                You are writing a two-host educational podcast script discussing the notebook "%s", in %s.
                Base it strictly on the sources below; keep it conversational, engaging, and accurate.

                Sources:
                %s

                %s

                %s

                %s

                Format EVERY line as either:
                HOST_A: <line of dialogue>
                HOST_B: <line of dialogue>
                Alternate speakers naturally. No stage directions, no headers, no commentary outside these lines.
                """.formatted(nb.getTitle(), language, context, formatInstruction, lengthInstruction, focusInstruction);

        String script = openAiService.chat(systemPrompt, "Generate the podcast script now.");
        if (script == null || (!script.contains("HOST_A:") && !script.contains("HOST_B:"))) {
            throw new RuntimeException("Audio script generation failed: AI response did not contain HOST_A/HOST_B lines");
        }
        return script;
    }

    private String normalizeAudioFormat(String format) {
        if (format == null) {
            return "deepdive";
        }
        String f = format.trim().toLowerCase();
        return switch (f) {
            case "brief", "critique", "debate", "deepdive" -> f;
            default -> "deepdive";
        };
    }

    private String normalizeAudioLength(String length) {
        if (length == null) {
            return "default";
        }
        String l = length.trim().toLowerCase();
        return switch (l) {
            case "short", "long", "default" -> l;
            default -> "default";
        };
    }

    /**
     * @param format "detaileddeck" (default) = full text/detail per slide, comprehensive,
     *               suitable for emailing or reading standalone.
     *               "presenterslides" = clean, shorter per-slide text — key talking points
     *               only, meant to support someone speaking rather than be read on its own.
     * @param length "short" = roughly 4-6 slides. "default" = 6-12 slides.
     */
    private String normalizeSlideDeckFormat(String format) {
        if (format == null) {
            return "detaileddeck";
        }
        String f = format.trim().toLowerCase();
        return switch (f) {
            case "detaileddeck", "presenterslides" -> f;
            default -> "detaileddeck";
        };
    }

    private String normalizeSlideDeckLength(String length) {
        if (length == null) {
            return "default";
        }
        String l = length.trim().toLowerCase();
        return switch (l) {
            case "short", "default" -> l;
            default -> "default";
        };
    }

    private String normalizeCountLevel(String level) {
        if (level == null) {
            return "standard";
        }
        String l = level.trim().toLowerCase();
        return switch (l) {
            case "fewer", "standard", "more" -> l;
            default -> "standard";
        };
    }

    private String normalizeDifficulty(String difficulty) {
        if (difficulty == null) {
            return "medium";
        }
        String d = difficulty.trim().toLowerCase();
        return switch (d) {
            case "easy", "medium", "hard" -> d;
            default -> "medium";
        };
    }
 // Slide Deck's "Default" length target range when no custom description is
 // given. Below MIN modules, we still generate at least MIN slides (adding
 // intro/summary framing as needed). At or below MAX modules, coverage is
 // 1 slide per module. Above MAX modules, modules are grouped evenly across
 // exactly MAX slides — never dropped, never left as bare one-line lists —
 // so slide count stays predictable no matter how large the source is.
    private static final int SLIDE_DECK_DEFAULT_MIN_SLIDES = 6;
    private static final int SLIDE_DECK_DEFAULT_MAX_SLIDES = 12;

 /**
  * Same as generateSlideJson(Notebook, String, List) used by Video, but
  * with Slide Deck's own format ("detaileddeck"/"presenterslides"),
  * length ("short"/"default"), and free-text description guidance baked
  * into the prompt. The resulting JSON still gets rendered into a real
  * .pptx via the same notebookSlideService.generate(...) call Video and
  * the original Slides flow already use — only the content/prompt
  * differs, not the rendering.
  *
  * Default length (no custom description) targets a fixed 6-20 slide
  * range regardless of source size: 1 slide per module when the source
  * has 20 or fewer modules/sections, or modules evenly grouped across
  * exactly 20 slides (each still individually named + described) when
  * there are more than 20 — so slide count stays predictable and no
  * module is ever silently dropped. A custom description overrides this
  * and drives scope/length itself.
  */
 private JsonNode generateSlideDeckJson(Notebook nb, String language, String format, String length,
                                         String description, List<Long> sourceIds) {
     String context = buildSourcesContext(nb, sourceIds);
     String resolvedFormat = normalizeSlideDeckFormat(format);
     String resolvedLength = normalizeSlideDeckLength(length);
     String resolvedDescription = (description == null) ? "" : description.trim();
     boolean hasCustomDescription = !resolvedDescription.isEmpty();

     String formatInstruction = "presenterslides".equals(resolvedFormat)
             ? "Format: PRESENTER SLIDES. Keep the on-slide bullets minimal — short talking points "
                     + "only, meant to support someone speaking rather than be read on its own. Put "
                     + "the fuller explanation in speakerNotes instead."
             : "Format: DETAILED DECK. Give each slide comprehensive text/detail in its bullets — "
                     + "this deck needs to stand alone for emailing or reading without a live "
                     + "presenter.";

     // No description given → full coverage within a fixed, predictable slide
     // range: 1 slide per module up to the max, or evenly grouped (never
     // dropped) beyond it. A description IS given → the user has told us
     // exactly what they want, so their wording drives scope/length instead
     // (it may deliberately narrow things down, e.g. "just cover joins and
     // indexing").
     String lengthInstruction;
     if (hasCustomDescription) {
         lengthInstruction = "short".equals(resolvedLength)
                 ? "Length: SHORT. Keep this brief — roughly 4-6 slides — while still following the "
                         + "description below for what to cover."
                 : "Length: DEFAULT. Let the description below determine scope and length; don't "
                         + "artificially pad or cut slides beyond what it calls for.";
     } else {
         lengthInstruction = "short".equals(resolvedLength)
                 ? "Length: SHORT. Generate roughly 4-6 slides covering only the highest-level themes."
                 : "Length: DEFAULT. Target between " + SLIDE_DECK_DEFAULT_MIN_SLIDES + " and "
                         + SLIDE_DECK_DEFAULT_MAX_SLIDES + " slides total (including the opening "
                         + "overview slide and closing summary slide). Determine coverage as follows:\n"
                         + "  1. If the sources contain a clear list of modules, sections, chapters, "
                         + "or units, and that count is " + SLIDE_DECK_DEFAULT_MAX_SLIDES + " or "
                         + "fewer, give each module its OWN slide — one module per slide, none "
                         + "skipped or merged.\n"
                         + "  2. If that module/section count is greater than "
                         + SLIDE_DECK_DEFAULT_MAX_SLIDES + ", you must still name and cover every "
                         + "single module somewhere in the deck. Distribute all modules evenly across "
                         + "exactly " + SLIDE_DECK_DEFAULT_MAX_SLIDES + " content slides (e.g. 50 "
                         + "modules across 20 slides ≈ 2-3 modules per slide). On each grouped slide, "
                         + "structure the bullets so every module gets its OWN bullet stating the "
                         + "module name, followed immediately by a second bullet that is a genuine "
                         + "1-2 sentence description of what that module covers — never just a bare "
                         + "list of module names with no explanation. Example bullet pattern for a "
                         + "slide covering 2 modules:\n"
                         + "     [\"Module 5: Indexing Strategies\", \"Covers B-tree and hash "
                         + "indexes, and when to use each for query performance.\", \"Module 6: "
                         + "Query Optimization\", \"Explains how the query planner chooses execution "
                         + "paths and how to read an EXPLAIN plan.\"]\n"
                         + "  3. If the sources have no clear module/section structure at all, cover "
                         + "the material comprehensively within the " + SLIDE_DECK_DEFAULT_MIN_SLIDES
                         + "-" + SLIDE_DECK_DEFAULT_MAX_SLIDES + " slide range using your own logical "
                         + "groupings.\n"
                         + "In all cases, never silently drop a module just to fit the slide count.";
     }

     String descriptionInstruction = hasCustomDescription
             ? "The user's specific request — follow this closely, including for scope and which "
                     + "topics to include or skip: " + resolvedDescription
             : "";

     String systemPrompt = """
             You are an AI study assistant generating presentation slides for a notebook titled "%s", in %s.
             Base them strictly on the sources below.

             Sources:
             %s

             %s

             %s

             %s

             Respond with ONLY raw JSON (no markdown fences, no commentary): an array of objects matching EXACTLY this shape:
             [
               {
                 "title": "Slide title",
                 "bullets": ["Short bullet point", "Another bullet point"],
                 "speakerNotes": "What the presenter should say for this slide"
               }
             ]
             Start with a title/overview slide and end with a summary slide.
             """.formatted(nb.getTitle(), language, context, formatInstruction, lengthInstruction, descriptionInstruction);

     // Slide Deck's Default length can legitimately produce up to
     // SLIDE_DECK_DEFAULT_MAX_SLIDES slides with detailed per-module
     // bullets — well beyond what the default 3000-token JSON budget can
     // safely hold, so use a larger explicit cap here to avoid truncation.
     // Slide Deck's Default length can produce a large JSON payload with
     // per-slide bullets and speaker notes. gpt-3.5-turbo (the model
     // configured via openai.model) hard-rejects any max_tokens above 4096,
     // so this must stay comfortably under that — 3500 leaves headroom for
     // the system prompt + source context to also fit within the model's
     // total context window without a separate context-length error.
     JsonNode json = callOpenAiForStrictJson(systemPrompt, "Generate the slides now.", 3500);
     if (!json.isArray() || json.isEmpty() || !json.get(0).has("title")) {
         throw new RuntimeException("Slide generation failed: AI response did not match the expected slide shape");
     }
     return json;
 }
    /**
     * Used by Video Overview so it supports the same source-selection
     * behavior as Audio Overview. Unchanged from before this task.
     */
    private JsonNode generateSlideJson(Notebook nb, String language, List<Long> sourceIds) {
        String context = buildSourcesContext(nb, sourceIds);
        String systemPrompt = """
                You are an AI study assistant generating presentation slides for a notebook titled "%s", in %s.
                Base them strictly on the sources below.

                Sources:
                %s

                Respond with ONLY raw JSON (no markdown fences, no commentary): an array of objects matching EXACTLY this shape:
                [
                  {
                    "title": "Slide title",
                    "bullets": ["Short bullet point", "Another bullet point"],
                    "speakerNotes": "What the presenter should say for this slide"
                  }
                ]
                Generate between 6 and 12 slides, starting with a title/overview slide and ending with a summary slide.
                """.formatted(nb.getTitle(), language, context);

        JsonNode json = callOpenAiForStrictJson(systemPrompt, "Generate the slides now.");
        if (!json.isArray() || json.isEmpty() || !json.get(0).has("title")) {
            throw new RuntimeException("Slide generation failed: AI response did not match the expected slide shape");
        }
        return json;
    }

    /**
     * Ownership-checked entry point for the topic-suggestions endpoint: looks
     * up the notebook the same way every other studio endpoint does, then
     * delegates to generateTopicSuggestions(Notebook, String). This is the
     * single shared implementation for Audio/Video's focus suggestions and
     * Flashcards/Quiz's topic suggestions — Mind Map and Data Table use
     * static placeholder example text on the frontend instead and don't call
     * this.
     */
    @Transactional(readOnly = true)
    public List<String> generateTopicSuggestions(Long notebookId, String studentEmail, String studioType) {
        Notebook nb = notebookRepository
                .findByIdAndStudentEmail(notebookId, studentEmail)
                .orElseThrow(() -> new RuntimeException("Notebook not found"));
        return generateTopicSuggestions(nb, studioType);
    }

    /**
     * Makes one lightweight OpenAI chat call asking for exactly 3 short (2-4
     * word) topic suggestions genuinely relevant to the notebook's real
     * source content, phrased appropriately for the given studioType (e.g.
     * "video"/"audio" suggestions read like focus topics to emphasize;
     * "flashcards" suggestions read like study topics; "quiz" suggestions
     * read like quiz angles). Used to populate content-aware suggestion
     * chips across several studio customize modals.
     */
    public List<String> generateTopicSuggestions(Notebook nb, String studioType) {
        String context = buildSourcesContext(nb);
        String systemPrompt = """
                You suggest short topic chips for a notebook studio customization
                modal. The notebook is titled "%s" and the user is about to generate a
                %s output for it.

                Sources:
                %s

                Suggest exactly 3 short, specific topics from the sources above that
                would make good suggestion chips for this generation — each genuinely
                present in the source content, not generic filler. Phrase them
                appropriately for a %s output (e.g. flashcards/quiz suggestions should
                read like study topics or quiz angles; audio/video suggestions should
                read like focus topics to emphasize).

                Respond with ONLY raw JSON (no markdown fences, no commentary): an
                array of exactly 3 short strings, each 2-4 words, e.g.:
                ["Topic one", "Topic two", "Topic three"]
                """.formatted(nb.getTitle(), studioType, context, studioType);

        JsonNode json = callOpenAiForStrictJson(systemPrompt, "Generate the topic suggestions now.");
        if (!json.isArray() || json.isEmpty()) {
            throw new RuntimeException("Topic suggestion generation failed: AI response was not a JSON array");
        }

        List<String> suggestions = new ArrayList<>();
        json.forEach(node -> {
            String text = node.asText("");
            if (!text.isBlank()) {
                suggestions.add(text.trim());
            }
        });
        if (suggestions.isEmpty()) {
            throw new RuntimeException("Topic suggestion generation failed: AI response contained no usable suggestions");
        }
        return suggestions;
    }

    /**
     * Generates a single AI infographic image summarizing the notebook's
     * sources: asks OpenAI to distill the sources into a short, concrete
     * image-generation prompt (incorporating the requested style, level of
     * detail, and any free-text guidance), generates the image via
     * OpenAiService, and uploads the resulting PNG to S3.
     */
    private InfographicResult generateInfographic(Notebook nb, Long notebookId, String language,
                                                   String style, String aspectRatio,
                                                   String levelOfDetail, String guidance) {
        String context = buildSourcesContext(nb);

        String resolvedStyle = (style == null || style.isBlank()) ? "auto-select" : style;
        String resolvedAspectRatio = (aspectRatio == null || aspectRatio.isBlank()) ? "landscape" : aspectRatio;
        String resolvedLevelOfDetail = (levelOfDetail == null || levelOfDetail.isBlank()) ? "standard" : levelOfDetail;
        String resolvedGuidance = (guidance == null || guidance.isBlank()) ? "none" : guidance;

        String systemPrompt = """
                You are a prompt writer for an AI image generator. Your only job is to output ONE
                image-generation prompt for a single infographic poster — nothing else.

                Topic: "%s" (notebook language: %s — write the prompt in English regardless, since
                the image model renders text best in English; note in the prompt that any on-image
                text should appear in %s if that is not English).

                Source material to summarize:
                %s

                Requirements for the prompt you write:
                1. Extract exactly 3 to 5 concrete facts, numbers, or stats from the sources above.
                   Never invent facts that aren't grounded in the sources.
                2. Describe a SINGLE poster-style infographic layout: a clear title area at the top,
                   then 3-5 distinct visual sections/icons/panels — one per fact — arranged in a
                   clean grid or vertical flow. Name the layout style explicitly (e.g. "grid of icon
                   cards", "vertical timeline", "central hub with radiating callouts").
                3. Visual style: %s. Translate this into concrete art-direction language an image
                   model can act on (e.g. "kawaii" → soft pastel colors, rounded shapes, cute simple
                   mascot character; "clay" → 3D claymation textures, matte lighting, soft shadows;
                   "sketch-note" → hand-drawn doodle style, black ink line art on off-white
                   background, marker-style highlights; "anime" → bold outlines, cel-shading, vibrant
                   saturated palette; "auto-select" → pick a clean modern flat-design style with a
                   cohesive 2-3 color palette).
                4. Level of detail: %s. "Concise" = big bold numbers/icons, minimal text, lots of
                   white space. "Standard" = balanced icon + short label + one supporting line per
                   section. "Detailed" = icon + label + 1-2 sentence explanation per section, still
                   uncluttered.
                5. Additional user guidance to incorporate (ignore if "none"): %s
                6. Specify a consistent color palette (2-4 colors) and background treatment.
                7. Do not describe any real people, brand logos, or copyrighted characters.

                Output ONLY the finished image-generation prompt itself: 4-8 concrete, visual
                sentences. No preamble, no markdown, no headers, no explanation of your choices,
                no quotation marks around it — just the prompt text ready to send directly to an
                image generator.
                """.formatted(nb.getTitle(), language, language, context,
                        resolvedStyle, resolvedLevelOfDetail, resolvedGuidance);

        String imagePrompt = openAiService.chat(systemPrompt, "Generate the infographic image prompt now.");
        if (imagePrompt == null || imagePrompt.isBlank()) {
            throw new RuntimeException("Infographic generation failed: could not produce an image prompt from the sources");
        }
        imagePrompt = imagePrompt.trim();

        byte[] imageBytes;
        try {
            imageBytes = openAiService.generateImage(imagePrompt, sizeForAspectRatio(resolvedAspectRatio));
        } catch (RuntimeException e) {
            throw new RuntimeException("Infographic generation failed: " + e.getMessage(), e);
        }

        String s3Key = S3Service.NOTEBOOK_KEY_PREFIX + "infographic/" + notebookId + "/" + UUID.randomUUID() + ".png";
        try {
            s3Service.uploadBytes(s3Key, imageBytes, "image/png");
        } catch (Exception e) {
            throw new RuntimeException("Infographic generation failed: could not upload the generated image", e);
        }

        return new InfographicResult(imagePrompt, s3Key);
    }

    private String sizeForAspectRatio(String aspectRatio) {
        if (aspectRatio == null) {
            return "1536x1024";
        }
        return switch (aspectRatio.trim().toLowerCase()) {
            case "portrait" -> "1024x1536";
            case "square" -> "1024x1024";
            default -> "1536x1024"; // landscape and any unrecognized value
        };
    }

    private record InfographicResult(String imagePrompt, String s3Key) {}

    // ── STRICT JSON HELPERS ─────────────────────────────────────────

    // Token cap used for callOpenAiForStrictJson's default (unspecified-limit)
    // calls — comfortably covers quiz/flashcard batches, mind maps, and
    // report-format suggestions.
    private static final int STRICT_JSON_DEFAULT_MAX_TOKENS = 3000;

    private JsonNode callOpenAiForStrictJson(String systemPrompt, String userMessage) {
        return callOpenAiForStrictJson(systemPrompt, userMessage, STRICT_JSON_DEFAULT_MAX_TOKENS);
    }

    /**
     * Same as {@link #callOpenAiForStrictJson(String, String)} but with an
     * explicit completion token cap, for JSON payloads that can legitimately
     * run large (e.g. a multi-slide deck with per-slide bullets and speaker
     * notes) and would otherwise get truncated mid-response.
     */
    private JsonNode callOpenAiForStrictJson(String systemPrompt, String userMessage, int maxTokens) {
        String raw = openAiService.chat(systemPrompt, userMessage, maxTokens);
        JsonNode parsed = tryParseJson(raw);
        if (parsed != null) {
            return parsed;
        }

        // Retry once with a stricter prompt
        String stricterSystemPrompt = systemPrompt + "\n\nIMPORTANT: Your previous response could not be parsed as JSON. "
                + "Respond with ONLY raw JSON — no markdown code fences, no commentary, no explanation before or after.";
        String retryRaw = openAiService.chat(stricterSystemPrompt, userMessage, maxTokens);
        JsonNode retryParsed = tryParseJson(retryRaw);
        if (retryParsed != null) {
            return retryParsed;
        }

        throw new RuntimeException("AI did not return valid JSON for this studio type, even after a retry");
    }

    private JsonNode tryParseJson(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.trim();
        // Strip markdown code fences if the model added them despite instructions
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(json)?", "").trim();
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3).trim();
            }
        }
        try {
            return objectMapper.readTree(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    // ── SOURCE CONTEXT (same pattern as NotebookChatService) ────────

    private String buildSourcesContext(Notebook nb) {
        return buildSourcesContext(nb, null);
    }

    /**
     * Same as buildSourcesContext(Notebook), but restricted to the sources
     * whose IDs are in sourceIds (IDs that don't belong to this notebook are
     * silently ignored). When sourceIds is null/empty, or when filtering
     * would leave zero sources (e.g. every ID was stale/invalid), this falls
     * back to using every source on the notebook.
     */
    private String buildSourcesContext(Notebook nb, List<Long> sourceIds) {
        List<NotebookSource> allSources = nb.getSources();
        List<NotebookSource> sources = allSources;

        if (sourceIds != null && !sourceIds.isEmpty()) {
            Set<Long> idSet = new HashSet<>(sourceIds);
            List<NotebookSource> filtered = (allSources == null)
                    ? Collections.emptyList()
                    : allSources.stream()
                            .filter(s -> idSet.contains(s.getId()))
                            .collect(Collectors.toList());
            // Never generate from zero context just because the requested IDs were bad.
            sources = filtered.isEmpty() ? allSources : filtered;
        }

        if (sources == null || sources.isEmpty()) {
            return "No sources added yet. Answer based on the notebook topic: " + nb.getTitle();
        }

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

        Collections.reverse(blocks);

        String context = String.join("\n\n", blocks);
        if (truncatedAny) {
            context += "\n\n[Note: some source content above was truncated or omitted to fit the "
                     + "assistant's context budget. Older sources were truncated first.]";
        }
        return context;
    }
}