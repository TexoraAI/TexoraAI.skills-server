package com.lms.live_session.controller;
import com.lms.live_session.repository.SessionAiNoteRepository;
import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.AiChatResponse;
import com.lms.live_session.service.AiCompanionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
@RestController
@RequestMapping("/api/v1/ai-companion/sessions")
public class AiSessionNotesController {

  

    
        private final AiCompanionService aiService;
        private final SessionAiNoteRepository sessionAiNoteRepository;
        public AiSessionNotesController(AiCompanionService aiService, SessionAiNoteRepository sessionAiNoteRepository) {
            this.aiService = aiService;
            this.sessionAiNoteRepository = sessionAiNoteRepository;
        }
 // POST /api/v1/ai-companion/sessions/{sessionId}/notes/generate
    @PostMapping("/{sessionId}/notes/generate")
    public ResponseEntity<AiChatResponse> generateNotes(
            @PathVariable Long sessionId,
            Principal principal,
            HttpServletRequest httpRequest) {
        AiChatRequest req = new AiChatRequest();
        req.setSessionId(sessionId);
        req.setMode("SUMMARIZER");
        req.setSaveToHistory(true);
        String userEmail = principal != null ? principal.getName() : null;

        String rawToken = null;
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            rawToken = authHeader.substring(7);
        }

        return ResponseEntity.ok(aiService.processRequest(req, userEmail, rawToken));
    }

    // GET /api/v1/ai-companion/sessions/{sessionId}/notes
    // GET /api/v1/ai-companion/sessions/{sessionId}/notes
    @GetMapping("/{sessionId}/notes")
    public ResponseEntity<?> getNotes(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionAiNoteRepository.findBySessionIdOrderByCreatedAtDesc(sessionId));
    }

    // GET /api/v1/ai-companion/sessions/{sessionId}/action-items
    @GetMapping("/{sessionId}/action-items")
    public ResponseEntity<?> getActionItems(@PathVariable Long sessionId) {
        // TODO: return sessionActionItemRepository.findBySessionIdOrderByCreatedAtDesc(sessionId)
        return ResponseEntity.ok(List.of());
    }
}