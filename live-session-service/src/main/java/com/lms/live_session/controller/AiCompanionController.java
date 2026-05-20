

package com.lms.live_session.controller;
 
import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.AiChatResponse;
import com.lms.live_session.service.AiCompanionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.security.Principal;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/v1/ai-companion")
public class AiCompanionController {
 
    private final AiCompanionService aiService;
 
    public AiCompanionController(AiCompanionService aiService) {
        this.aiService = aiService;
    }
 
    /**
     * POST /api/v1/ai-companion/chat
     * Body: AiChatRequest (upgraded — includes sources, resourceIds, conversationId)
     * Auth: JWT required
     *
     * Frontend validation enforced here:
     * - If sessionId arrives as null it's accepted for general modes
     * - If sessionId is required for the mode, AiCompanionService returns a friendly error
     */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
        @RequestBody AiChatRequest request,
        Principal principal   // JWT principal — user email from token
    ) {
        // Security: do not trust sessionId==0 or negative from frontend
        if (request.getSessionId() != null && request.getSessionId() <= 0) {
            request.setSessionId(null);
        }
 
        // The principal.getName() returns the JWT subject (email) — use in service for logs
        // TODO: pass principal.getName() to service for activity logging when AiActivityLogService exists
        // String userEmail = principal != null ? principal.getName() : "unknown";
 
        try {
            AiChatResponse response = aiService.processRequest(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(AiChatResponse.error("Failed: " + e.getMessage()));
        }
    }
 
    /** GET /api/v1/ai-companion/modes */
    @GetMapping("/modes")
    public ResponseEntity<List<Map<String, String>>> getModes() {
        return ResponseEntity.ok(aiService.getAvailableModes());
    }
 
    /** GET /api/v1/ai-companion/health */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "ILM ORA AI Companion is running",
            "version", "2.0"
        ));
    }
}