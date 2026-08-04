package com.lms.live_session.controller;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.*;
import com.lms.live_session.entity.AiConversation;
import com.lms.live_session.entity.AiMessage;
import com.lms.live_session.repository.AiConversationRepository;
import com.lms.live_session.repository.AiMessageRepository;
@RestController
@RequestMapping("/api/v1/ai-companion/conversations")
public class AiConversationController {
 
    // @Autowired AiConversationService conversationService;
 
    // POST /api/v1/ai-companion/conversations
	private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;

    public AiConversationController(AiConversationRepository conversationRepository,
                                     AiMessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }
    // Creates a new empty conversation
//    @PostMapping
//    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Principal principal) {
//        // TODO: return conversationService.create(principal.getName(), body)
//        return ResponseEntity.ok(Map.of("id", 1L, "status", "ACTIVE"));
//    }
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Principal principal) {
        AiConversation conversation = new AiConversation();
        conversation.setUserEmail(principal.getName());
        if (body.get("sessionId") != null) {
            conversation.setSessionId(Long.valueOf(body.get("sessionId").toString()));
        }
        conversation.setTitle(body.get("title") != null ? body.get("title").toString() : "New Conversation");
        conversation.setStatus("ACTIVE");
        conversation = conversationRepository.save(conversation);
        return ResponseEntity.ok(conversation);
    }
 
    // GET /api/v1/ai-companion/conversations
    // Returns all conversations for authenticated user
//    @GetMapping
//    public ResponseEntity<?> list(Principal principal) {
//        // TODO: return conversationService.listByUser(principal.getName())
//        return ResponseEntity.ok(List.of());
//    }
// 
    @GetMapping
    public ResponseEntity<?> list(Principal principal) {
        return ResponseEntity.ok(
            conversationRepository.findByUserEmailOrderByUpdatedAtDesc(principal.getName())
        );
    }
    // GET /api/v1/ai-companion/conversations/{id}
//    @GetMapping("/{id}")
//    public ResponseEntity<?> get(@PathVariable Long id, Principal principal) {
//        // TODO: return conversationService.getById(id, principal.getName())
//        return ResponseEntity.ok(Map.of("id", id));
//    }
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, Principal principal) {
        Optional<AiConversation> convo = conversationRepository.findById(id);
        if (convo.isEmpty()) return ResponseEntity.notFound().build();
        if (!convo.get().getUserEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(convo.get());
    }
 
//    // GET /api/v1/ai-companion/conversations/{id}/messages
//    @GetMapping("/{id}/messages")
//    public ResponseEntity<?> getMessages(@PathVariable Long id, Principal principal) {
//        // TODO: return conversationService.getMessages(id, principal.getName())
//        return ResponseEntity.ok(List.of());
//    }
    @GetMapping("/{id}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long id, Principal principal) {
        Optional<AiConversation> convo = conversationRepository.findById(id);
        if (convo.isEmpty()) return ResponseEntity.notFound().build();
        if (!convo.get().getUserEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(
            messageRepository.findByConversationIdOrderByCreatedAtAsc(id)
        );
    }
 
//    // DELETE /api/v1/ai-companion/conversations/{id}
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
//        // TODO: conversationService.delete(id, principal.getName())
//        return ResponseEntity.noContent().build();
//    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Principal principal) {
        Optional<AiConversation> convo = conversationRepository.findById(id);
        if (convo.isEmpty()) return ResponseEntity.notFound().build();
        if (!convo.get().getUserEmail().equals(principal.getName())) {
            return ResponseEntity.status(403).build();
        }
        messageRepository.deleteByConversationId(id);
        conversationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}