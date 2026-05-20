package com.lms.live_session.controller;
 
import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.AiChatResponse;
import com.lms.live_session.service.AiCompanionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/v1/ai-companion/sessions")
public class AiSessionNotesController {
 
    private final AiCompanionService aiService;
 
    public AiSessionNotesController(AiCompanionService aiService) {
        this.aiService = aiService;
    }
 
    // POST /api/v1/ai-companion/sessions/{sessionId}/notes/generate
    @PostMapping("/{sessionId}/notes/generate")
    public ResponseEntity<AiChatResponse> generateNotes(@PathVariable Long sessionId) {
        AiChatRequest req = new AiChatRequest();
        req.setSessionId(sessionId);
        req.setMode("SUMMARIZER");
        req.setSaveToHistory(true);
        return ResponseEntity.ok(aiService.processRequest(req));
    }
 
    // GET /api/v1/ai-companion/sessions/{sessionId}/notes
    @GetMapping("/{sessionId}/notes")
    public ResponseEntity<?> getNotes(@PathVariable Long sessionId) {
        // TODO: return sessionAiNoteRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
        return ResponseEntity.ok(List.of());
    }
 
    // GET /api/v1/ai-companion/sessions/{sessionId}/action-items
    @GetMapping("/{sessionId}/action-items")
    public ResponseEntity<?> getActionItems(@PathVariable Long sessionId) {
        // TODO: return sessionActionItemRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
        return ResponseEntity.ok(List.of());
    }
}