package com.lms.chat.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.lms.chat.constants.ChatFeatureKeys;
import com.lms.chat.dto.*;
import com.lms.chat.entity.NotebookChatMessage;
import com.lms.chat.service.ChatFeatureFlagsService;
import com.lms.chat.service.NotebookChatService;
import com.lms.chat.service.NotebookService;
import com.lms.chat.service.NotebookUsageService;
import com.lms.chat.service.NotebookStudioService;
import com.lms.chat.service.NotebookSharingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notebooks")
public class NotebookController {

    private final NotebookService notebookService;
    private final NotebookChatService notebookChatService;
    private final ChatFeatureFlagsService chatFeatureFlagsService;
    private final NotebookUsageService notebookUsageService;
    private final NotebookStudioService notebookStudioService;
    private final NotebookSharingService notebookSharingService;

    public NotebookController(NotebookService notebookService,
                               NotebookChatService notebookChatService,
                               ChatFeatureFlagsService chatFeatureFlagsService,
                               NotebookUsageService notebookUsageService,
                               NotebookStudioService notebookStudioService,
                               NotebookSharingService notebookSharingService) {
        this.notebookService = notebookService;
        this.notebookChatService = notebookChatService;
        this.chatFeatureFlagsService = chatFeatureFlagsService;
        this.notebookUsageService = notebookUsageService;
        this.notebookStudioService = notebookStudioService;
        this.notebookSharingService = notebookSharingService;
    }

    // Added — matches ChatController/FeedbackController pattern.
    // Was missing before; needed so enforce() can resolve org-or-email scope.
    private String organizationId(Authentication auth) {
        Object details = auth.getDetails();
        return details == null ? null : details.toString();
    }

    // ── NOTEBOOK ──────────────────────────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<List<NotebookResponse>> getMyNotebooks(Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_MY_NOTEBOOKS);
        return ResponseEntity.ok(notebookService.getMyNotebooks(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotebookResponse> getNotebook(
            @PathVariable Long id, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_NOTEBOOK);
        return ResponseEntity.ok(notebookService.getNotebook(id, auth.getName()));
    }

    @PostMapping
    public ResponseEntity<NotebookResponse> createNotebook(
            @RequestBody NotebookRequest req, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.CREATE_NOTEBOOK);
        return ResponseEntity.ok(notebookService.createNotebook(req, auth.getName(), organizationId(auth)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotebookResponse> updateNotebook(
            @PathVariable Long id,
            @RequestBody NotebookRequest req,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.UPDATE_NOTEBOOK);
        return ResponseEntity.ok(notebookService.updateNotebook(id, req, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotebook(
            @PathVariable Long id, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.DELETE_NOTEBOOK);
        notebookService.deleteNotebook(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    // ── SECTION ───────────────────────────────────────────────────

    @PostMapping("/sections")
    public ResponseEntity<NotebookResponse> addSection(
            @RequestBody NotebookSectionRequest req, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.ADD_SECTION);
        return ResponseEntity.ok(notebookService.addSection(req, auth.getName(), organizationId(auth)));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<NotebookResponse> updateSection(
            @PathVariable Long id,
            @RequestBody NotebookSectionRequest req,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.UPDATE_SECTION);
        return ResponseEntity.ok(notebookService.updateSection(id, req, auth.getName()));
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<?> deleteSection(
            @PathVariable Long id, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.DELETE_SECTION);
        try {
            return ResponseEntity.ok(notebookService.deleteSection(id, auth.getName()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ── PAGE ──────────────────────────────────────────────────────

    @PostMapping("/pages")
    public ResponseEntity<NotebookResponse> addPage(
            @RequestBody NotebookPageRequest req, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.ADD_PAGE);
        return ResponseEntity.ok(notebookService.addPage(req, auth.getName(), organizationId(auth)));
    }

    @PutMapping("/pages/{id}")
    public ResponseEntity<NotebookPageResponse> savePage(
            @PathVariable Long id,
            @RequestBody NotebookPageRequest req,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.SAVE_PAGE);
        return ResponseEntity.ok(notebookService.savePage(id, req, auth.getName()));
    }

    @DeleteMapping("/pages/{id}")
    public ResponseEntity<?> deletePage(
            @PathVariable Long id, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.DELETE_PAGE);
        try {
            return ResponseEntity.ok(notebookService.deletePage(id, auth.getName()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // ── SOURCES ───────────────────────────────────────────────────

    @PostMapping("/{id}/sources/url")
    public ResponseEntity<NotebookResponse> addUrlSource(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.ADD_URL_SOURCE);
        return ResponseEntity.ok(
            notebookService.addUrlSource(id, body.get("url"), auth.getName(), organizationId(auth))
        );
    }

    @PostMapping("/{id}/sources/file")
    public ResponseEntity<NotebookResponse> addFileSource(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.ADD_FILE_SOURCE);
        return ResponseEntity.ok(
            notebookService.addFileSource(id, file, auth.getName(), organizationId(auth))
        );
    }

    @DeleteMapping("/sources/{sourceId}")
    public ResponseEntity<NotebookResponse> deleteSource(
            @PathVariable Long sourceId,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.DELETE_SOURCE);
        return ResponseEntity.ok(
            notebookService.deleteSource(sourceId, auth.getName())
        );
    }

    // ── NOTEBOOK AI CHAT ──────────────────────────────────────────

    @PostMapping("/{id}/chat")
    public ResponseEntity<NotebookChatResponse> chat(
            @PathVariable Long id,
            @RequestBody NotebookChatRequest req,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.NOTEBOOK_AI_CHAT);
        try {
            String reply = notebookChatService.chat(id, auth.getName(), req.getMessage(), organizationId(auth));
            return ResponseEntity.ok(new NotebookChatResponse(reply));
        } catch (Exception e) {
            return ResponseEntity.ok(
                new NotebookChatResponse("Sorry, I couldn't process that. Please try again.")
            );
        }
    }

    // NOTE: reuses the NOTEBOOK_AI_CHAT feature flag key since this is just the
    // read side of the same chat feature. Swap for a dedicated key if the
    // project wants to gate history access separately from sending messages.
    @GetMapping("/{id}/chat/history")
    public ResponseEntity<List<NotebookChatMessageResponse>> getChatHistory(
            @PathVariable Long id, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.NOTEBOOK_AI_CHAT);
        List<NotebookChatMessage> history = notebookChatService.getHistory(id, auth.getName());
        List<NotebookChatMessageResponse> response = history.stream()
                .map(NotebookChatMessageResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ── STUDIO ────────────────────────────────────────────────────

    @PostMapping("/{id}/studio/{type}")
    public ResponseEntity<?> generateStudioOutput(
            @PathVariable Long id,
            @PathVariable String type,
            @RequestBody(required = false) NotebookStudioRequest body,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.NOTEBOOK_STUDIO_GENERATE);
        try {
            String language = (body != null && body.getLanguage() != null) ? body.getLanguage() : "en";
            // Infographic-only fields; other studio types ignore these and don't need to send them.
            String style = (body != null && body.getStyle() != null) ? body.getStyle() : "auto-select";
            String aspectRatio = (body != null && body.getAspectRatio() != null) ? body.getAspectRatio() : "landscape";
            String levelOfDetail = (body != null && body.getLevelOfDetail() != null) ? body.getLevelOfDetail() : "standard";
            String guidance = (body != null && body.getGuidance() != null) ? body.getGuidance() : "";
            // Shared between Audio, Video, and Slide Deck; other studio types ignore these.
            String format = (body != null && body.getFormat() != null) ? body.getFormat() : "deepdive";
            String focus = (body != null && body.getFocus() != null) ? body.getFocus() : "";
            List<Long> sourceIds = (body != null) ? body.getSourceIds() : null;
            // Shared between Audio and Slide Deck.
            String length = (body != null && body.getLength() != null) ? body.getLength() : "default";
            // Video-only field; other studio types ignore this and don't need to send it.
            String visualStyle = (body != null && body.getVisualStyle() != null) ? body.getVisualStyle() : "auto-select";
            // Reports-only: chosen format title (fixed/"Create Your Own"/AI-suggested).
            String formatTitle = (body != null && body.getFormatTitle() != null) ? body.getFormatTitle() : "";
            // Reports/Slide Deck/Data Table: free-text structure/style guidance.
            String description = (body != null && body.getDescription() != null) ? body.getDescription() : "";
            // Mind Map/Flashcards/Quiz: optional scoping topic.
            String topic = (body != null && body.getTopic() != null) ? body.getTopic() : "";
            // Flashcards-only card count level.
            String cardCountLevel = (body != null && body.getCardCountLevel() != null) ? body.getCardCountLevel() : "standard";
            // Quiz-only question count level.
            String questionCountLevel = (body != null && body.getQuestionCountLevel() != null) ? body.getQuestionCountLevel() : "standard";
            // Shared between Flashcards and Quiz.
            String difficulty = (body != null && body.getDifficulty() != null) ? body.getDifficulty() : "medium";

            // Returns immediately with a PENDING output; the actual generation
            // now runs in the background (see NotebookStudioService). The
            // frontend polls GET /notebooks/{id}/studio to see it flip to
            // READY/FAILED — same 200 response shape as before, just faster.
            NotebookStudioOutputResponse output = notebookStudioService.createPendingOutput(
                    id, type, language, auth.getName(), organizationId(auth),
                    style, aspectRatio, levelOfDetail, guidance,
                    format, length, focus, visualStyle, sourceIds,
                    formatTitle, description, topic, cardCountLevel, questionCountLevel, difficulty);
            return ResponseEntity.ok(output);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", e.getMessage()));
        }
    }

    // Populates content-aware report-format suggestion options for the
    // Reports customize modal: 4 { title, description } pairs tailored to
    // this notebook's actual source material.
    @GetMapping("/{id}/studio/report-format-suggestions")
    public ResponseEntity<?> getReportFormatSuggestions(
            @PathVariable Long id, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.NOTEBOOK_STUDIO_GENERATE);
        try {
            JsonNode suggestions = notebookStudioService.generateReportFormatSuggestions(id, auth.getName());
            return ResponseEntity.ok(suggestions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", e.getMessage()));
        }
    }

    // Populates content-aware topic/focus suggestion chips in studio customize
    // modals (Audio, Video, Flashcards, Quiz). Canonical route going forward;
    // ?type= selects phrasing (defaults to "video"). Mind Map and Data Table
    // use static placeholder text on the frontend instead and don't call this.
    @GetMapping("/{id}/studio/topic-suggestions")
    public ResponseEntity<?> getTopicSuggestions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "video") String type,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.NOTEBOOK_STUDIO_GENERATE);
        try {
            List<String> suggestions = notebookStudioService.generateTopicSuggestions(id, auth.getName(), type);
            return ResponseEntity.ok(suggestions);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", e.getMessage()));
        }
    }

    // Legacy route from the original Audio/Video-only focus-suggestions work.
    // Kept so any existing frontend calls keep working; delegates straight
    // into the canonical /topic-suggestions handler above rather than keeping
    // a second implementation.
    @GetMapping("/{id}/studio/focus-suggestions")
    public ResponseEntity<?> getFocusSuggestions(
            @PathVariable Long id,
            @RequestParam(defaultValue = "video") String studioType,
            Authentication auth) {
        return getTopicSuggestions(id, studioType, auth);
    }

    @GetMapping("/{id}/studio")
    public ResponseEntity<List<NotebookStudioOutputResponse>> getStudioOutputs(
            @PathVariable Long id, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.NOTEBOOK_STUDIO_LIST);
        return ResponseEntity.ok(notebookStudioService.listOutputs(id, auth.getName()));
    }

    @DeleteMapping("/studio/{outputId}")
    public ResponseEntity<Void> deleteStudioOutput(
            @PathVariable Long outputId, Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.NOTEBOOK_STUDIO_DELETE);
        notebookStudioService.deleteOutput(outputId, auth.getName());
        return ResponseEntity.noContent().build();
    }

    // ── SHARING ───────────────────────────────────────────────────

    @PostMapping("/{id}/share")
    public ResponseEntity<?> shareNotebook(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.NOTEBOOK_SHARE);
        try {
            notebookSharingService.share(id, body.get("email"), auth.getName());
            return ResponseEntity.ok(Map.of("message", "Notebook shared with " + body.get("email")));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    // ── USAGE ─────────────────────────────────────────────────────

    @GetMapping("/usage")
    public ResponseEntity<Map<String, Object>> getUsage(Authentication auth) {
        return ResponseEntity.ok(
            notebookUsageService.getUsageStatus(auth.getName(), organizationId(auth))
        );
    }
}