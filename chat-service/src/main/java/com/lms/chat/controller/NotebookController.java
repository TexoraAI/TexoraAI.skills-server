package com.lms.chat.controller;

import com.lms.chat.dto.*;
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

    public NotebookController(NotebookService notebookService,NotebookChatService notebookChatService) {
        this.notebookService = notebookService;
        this.notebookChatService = notebookChatService;
    }
    

    // ── NOTEBOOK ──────────────────────────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<List<NotebookResponse>> getMyNotebooks(Authentication auth) {
        return ResponseEntity.ok(notebookService.getMyNotebooks(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotebookResponse> getNotebook(
            @PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(notebookService.getNotebook(id, auth.getName()));
    }

    @PostMapping
    public ResponseEntity<NotebookResponse> createNotebook(
            @RequestBody NotebookRequest req, Authentication auth) {
        return ResponseEntity.ok(notebookService.createNotebook(req, auth.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NotebookResponse> updateNotebook(
            @PathVariable Long id,
            @RequestBody NotebookRequest req,
            Authentication auth) {
        return ResponseEntity.ok(notebookService.updateNotebook(id, req, auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotebook(
            @PathVariable Long id, Authentication auth) {
        notebookService.deleteNotebook(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    // ── SECTION ───────────────────────────────────────────────────

    @PostMapping("/sections")
    public ResponseEntity<NotebookResponse> addSection(
            @RequestBody NotebookSectionRequest req, Authentication auth) {
        return ResponseEntity.ok(notebookService.addSection(req, auth.getName()));
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<NotebookResponse> updateSection(
            @PathVariable Long id,
            @RequestBody NotebookSectionRequest req,
            Authentication auth) {
        return ResponseEntity.ok(notebookService.updateSection(id, req, auth.getName()));
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<?> deleteSection(
            @PathVariable Long id, Authentication auth) {
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
        return ResponseEntity.ok(notebookService.addPage(req, auth.getName()));
    }

    @PutMapping("/pages/{id}")
    public ResponseEntity<NotebookPageResponse> savePage(
            @PathVariable Long id,
            @RequestBody NotebookPageRequest req,
            Authentication auth) {
        return ResponseEntity.ok(notebookService.savePage(id, req, auth.getName()));
    }

    @DeleteMapping("/pages/{id}")
    public ResponseEntity<?> deletePage(
            @PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(notebookService.deletePage(id, auth.getName()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    
 // Add to NotebookController.java

    @PostMapping("/{id}/sources/url")
    public ResponseEntity<NotebookResponse> addUrlSource(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        return ResponseEntity.ok(
            notebookService.addUrlSource(id, body.get("url"), auth.getName())
        );
    }

    @PostMapping("/{id}/sources/file")
    public ResponseEntity<NotebookResponse> addFileSource(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        return ResponseEntity.ok(
            notebookService.addFileSource(id, file, auth.getName())
        );
    }

    @DeleteMapping("/sources/{sourceId}")
    public ResponseEntity<NotebookResponse> deleteSource(
            @PathVariable Long sourceId,
            Authentication auth) {
        return ResponseEntity.ok(
            notebookService.deleteSource(sourceId, auth.getName())
        );
    }
    
    @PostMapping("/{id}/chat")
    public ResponseEntity<NotebookChatResponse> chat(
            @PathVariable Long id,
            @RequestBody NotebookChatRequest req,
            Authentication auth) {
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