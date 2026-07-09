


package com.lms.chat.controller;

import com.lms.chat.constants.ChatFeatureKeys;
import com.lms.chat.dto.*;
import com.lms.chat.service.ChatFeatureFlagsService;
import com.lms.chat.service.NotebookChatService;
import com.lms.chat.service.NotebookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notebooks")
public class NotebookController {

    private final NotebookService notebookService;
    private final NotebookChatService notebookChatService;
    private final ChatFeatureFlagsService chatFeatureFlagsService;

    public NotebookController(NotebookService notebookService,
                               NotebookChatService notebookChatService,
                               ChatFeatureFlagsService chatFeatureFlagsService) {
        this.notebookService = notebookService;
        this.notebookChatService = notebookChatService;
        this.chatFeatureFlagsService = chatFeatureFlagsService;
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
        return ResponseEntity.ok(notebookService.createNotebook(req, auth.getName()));
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
        return ResponseEntity.ok(notebookService.addSection(req, auth.getName()));
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
        return ResponseEntity.ok(notebookService.addPage(req, auth.getName()));
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
            notebookService.addUrlSource(id, body.get("url"), auth.getName())
        );
    }

    @PostMapping("/{id}/sources/file")
    public ResponseEntity<NotebookResponse> addFileSource(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.ADD_FILE_SOURCE);
        return ResponseEntity.ok(
            notebookService.addFileSource(id, file, auth.getName())
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
            String reply = notebookChatService.chat(id, auth.getName(), req.getMessage());
            return ResponseEntity.ok(new NotebookChatResponse(reply));
        } catch (Exception e) {
            return ResponseEntity.ok(
                new NotebookChatResponse("Sorry, I couldn't process that. Please try again.")
            );
        }
    }
}