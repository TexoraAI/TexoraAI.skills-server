


package com.lms.chat.controller;

import java.util.Map;
import com.lms.chat.constants.ChatFeatureKeys;
import com.lms.chat.entity.ChatMessage;
import com.lms.chat.service.ChatService;
import com.lms.chat.service.ChatFeatureFlagsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService service;
    private final ChatFeatureFlagsService chatFeatureFlagsService;

    public ChatController(ChatService service, ChatFeatureFlagsService chatFeatureFlagsService) {
        this.service = service;
        this.chatFeatureFlagsService = chatFeatureFlagsService;
    }

    private String organizationId(Authentication auth) {
        Object details = auth.getDetails();
        return details == null ? null : details.toString();
    }

//    // ================= SEND MESSAGE =================
//    @PostMapping("/send")
//    public ChatMessage send(
//            @RequestBody ChatMessage message,
//            Authentication auth
//    ) {
//        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.SEND_MESSAGE);
//        String loggedUser = auth.getName();
//        return service.send(message, loggedUser, organizationId(auth));
//    }
//
//    // ================= GET CONVERSATION =================
//    @GetMapping("/conversation")
//    public List<ChatMessage> conversation(
//            @RequestParam Long batchId,
//            @RequestParam String otherUser,
//            Authentication auth
//    ) {
//        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_CONVERSATION);
//        return service.getConversation(batchId, auth.getName(), otherUser, organizationId(auth));
//    }
//
 // ================= SEND MESSAGE =================
    @PostMapping("/send")
    public ChatMessage send(
            @RequestBody ChatMessage message,
            Authentication auth
    ) {
        String featureKey = isTrainer(auth)
                ? ChatFeatureKeys.SEND_MESSAGE_TRAINER
                : ChatFeatureKeys.SEND_MESSAGE_STUDENT;
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), featureKey);
        String loggedUser = auth.getName();
        return service.send(message, loggedUser, organizationId(auth));
    }

    // ================= GET CONVERSATION =================
    @GetMapping("/conversation")
    public List<ChatMessage> conversation(
            @RequestParam Long batchId,
            @RequestParam String otherUser,
            Authentication auth
    ) {
        String featureKey = isTrainer(auth)
                ? ChatFeatureKeys.GET_CONVERSATION_TRAINER
                : ChatFeatureKeys.GET_CONVERSATION_STUDENT;
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), featureKey);
        return service.getConversation(batchId, auth.getName(), otherUser, organizationId(auth));
    }
    // ================= TRAINER STUDENTS LIST =================
    @GetMapping("/trainer/students")
    public List<String> trainerStudents(
            @RequestParam Long batchId,
            Authentication auth
    ) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_TRAINER_STUDENTS);
        return service.getTrainerStudents(batchId, auth.getName(), organizationId(auth));
    }

    // ================= STUDENT TRAINER =================
    @GetMapping("/student/trainer")
    public String getStudentTrainer(
            @RequestParam Long batchId,
            Authentication auth
    ) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_STUDENT_TRAINER);
        return service.getStudentTrainer(batchId, auth.getName(), organizationId(auth));
    }

    @GetMapping("/student/context")
    public Map<String, Object> getStudentChatContext(Authentication auth) {
        chatFeatureFlagsService.enforce(organizationId(auth), auth.getName(), ChatFeatureKeys.GET_STUDENT_CONTEXT);
        return service.getStudentContext(auth.getName(), organizationId(auth));
    }
    
 // Added for role-aware feature key resolution on shared endpoints
 // (send/conversation are used by both trainer and student).
 private boolean isTrainer(Authentication auth) {
     return auth.getAuthorities().stream()
             .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_TRAINER"));
 }
}