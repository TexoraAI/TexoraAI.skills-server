

package com.lms.live_session.controller;

import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.AiChatResponse;
import com.lms.live_session.service.AiCompanionService;
import jakarta.servlet.http.HttpServletRequest;
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
        Principal principal,        // JWT principal — user email from token
        HttpServletRequest httpRequest
    ) {
        // Security: do not trust sessionId==0 or negative from frontend
        if (request.getSessionId() != null && request.getSessionId() <= 0) {
            request.setSessionId(null);
        }

        // The principal.getName() returns the JWT subject (email) — used in service for logs
        String userEmail = principal != null ? principal.getName() : null;

        // Extract raw JWT so the service can pull organizationId from it for the org check
        String rawToken = null;
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            rawToken = authHeader.substring(7);
        }
//
//        try {
//            AiChatResponse response = aiService.processRequest(request, userEmail, rawToken);
//
//            // Quota-exceeded responses come back as a 200-shaped AiChatResponse with a
//            // QUOTA_BLOCKED: prefix in the error field. Map that to a proper HTTP status
//            // instead of letting it through as 200 OK.
//            if (response.getError() != null && response.getError().startsWith("QUOTA_BLOCKED:")) {
//                return ResponseEntity.status(429).body(response);
//            }
//
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError()
//                .body(AiChatResponse.error("Failed: " + e.getMessage()));
//        }
//    }
        try {
            AiChatResponse response = aiService.processRequest(request, userEmail, rawToken);

            // Quota-exceeded responses come back as a 200-shaped AiChatResponse with a
            // QUOTA_BLOCKED: prefix in the error field. Map that to a proper HTTP status
            // instead of letting it through as 200 OK.
            if (response.getError() != null && response.getError().startsWith("QUOTA_BLOCKED:")) {
                return ResponseEntity.status(429).body(response);
            }

            return ResponseEntity.ok(response);
        } catch (com.lms.live_session.exception.AiCompanionNotAvailableException
                | com.lms.live_session.exception.AiCompanionUsageLimitExceededException e) {
            throw e; // let GlobalExceptionHandler produce the documented 403/429 + error code
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