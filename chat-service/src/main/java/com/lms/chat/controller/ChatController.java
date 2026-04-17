



//
//package com.lms.chat.controller;
//import java.util.Map;
//import com.lms.chat.entity.ChatMessage;
//import com.lms.chat.service.ChatService;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/chat")
//public class ChatController {
//
//    private final ChatService service;
//
//    public ChatController(ChatService service) {
//        this.service = service;
//    }
//
//    // ================= SEND MESSAGE =================
//    @PostMapping("/send")
//    public ChatMessage send(
//            @RequestBody ChatMessage message,
//            Authentication auth
//    ) {
//        String loggedUser = auth.getName(); // from JWT
//        return service.send(message, loggedUser);
//    }
//
//    // ================= GET CONVERSATION =================
//    @GetMapping("/conversation")
//    public List<ChatMessage> conversation(
//            @RequestParam Long batchId,
//            @RequestParam String otherUser,
//            Authentication auth
//    ) {
//        return service.getConversation(batchId, auth.getName(), otherUser);
//    }
//
//    // ================= TRAINER STUDENTS LIST =================
//    @GetMapping("/trainer/students")
//    public List<String> trainerStudents(
//            @RequestParam Long batchId,
//            Authentication auth
//    ) {
//        return service.getTrainerStudents(batchId, auth.getName());
//    }
// // ================= STUDENT TRAINER =================
//    @GetMapping("/student/trainer")
//    public String getStudentTrainer(
//            @RequestParam Long batchId,
//            Authentication auth
//    ) {
//        return service.getStudentTrainer(batchId, auth.getName());
//    }
//    
//    @GetMapping("/student/context")
//    public Map<String, Object> getStudentChatContext(Authentication auth) {
//        return service.getStudentContext(auth.getName());
//    }
//
//
//}
