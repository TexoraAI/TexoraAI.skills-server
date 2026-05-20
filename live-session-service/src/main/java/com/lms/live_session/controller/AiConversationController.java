package com.lms.live_session.controller;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;
 
@RestController
@RequestMapping("/api/v1/ai-companion/conversations")
public class AiConversationController {
 
    // @Autowired AiConversationService conversationService;
 
    // POST /api/v1/ai-companion/conversations
    // Creates a new empty conversation
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Principal principal) {
        // TODO: return conversationService.create(principal.getName(), body)
        return ResponseEntity.ok(Map.of("id", 1L, "status", "ACTIVE"));
    }
 
    // GET /api/v1/ai-companion/conversations
    // Returns all conversations for authenticated user
    @GetMapping
    public ResponseEntity<?> list(Principal principal) {
        // TODO: return conversationService.listByUser(principal.getName())
        return ResponseEntity.ok(List.of());
    }
 
    // GET /api/v1/ai-companion/conversations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, Principal principal) {
        // TODO: return conversationService.getById(id, principal.getName())
        return ResponseEntity.ok(Map.of("id", id));
    }
 
    // GET /api/v1/ai-companion/conversations/{id}/messages
    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long id, Principal principal) {
        // TODO: return conversationService.getMessages(id, principal.getName())
        return ResponseEntity.ok(List.of());
    }
 
    // DELETE /api/v1/ai-companion/conversations/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        // TODO: conversationService.delete(id, principal.getName())
        return ResponseEntity.noContent().build();
    }
}