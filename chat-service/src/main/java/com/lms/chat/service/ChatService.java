

package com.lms.chat.service;

import java.util.HashMap;
import java.util.Map;
import com.lms.chat.entity.ChatClassroomAccess;
import com.lms.chat.entity.ChatMessage;
import com.lms.chat.kafka.ChatEventProducer; // ✅ ADDED import
import com.lms.chat.repository.ChatClassroomAccessRepository;
import com.lms.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    private final ChatMessageRepository repository;
    private final ChatClassroomAccessRepository accessRepo;
    private final ChatEventProducer chatEventProducer; // ✅ ADDED

    public ChatService(ChatMessageRepository repository,
                       ChatClassroomAccessRepository accessRepo,
                       ChatEventProducer chatEventProducer) { // ✅ ADDED
        this.repository = repository;
        this.accessRepo = accessRepo;
        this.chatEventProducer = chatEventProducer; // ✅ ADDED
    }

    public ChatMessage send(ChatMessage msg, String loggedUser) {
        msg.setSenderEmail(loggedUser);

        boolean allowed =
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(
                        msg.getBatchId(),
                        msg.getReceiverEmail(),
                        loggedUser
                )
                ||
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(
                        msg.getBatchId(),
                        loggedUser,
                        msg.getReceiverEmail()
                );

        if (!allowed) {
            throw new RuntimeException("You are not assigned to this classroom");
        }

        msg.setSentAt(LocalDateTime.now());
        ChatMessage saved = repository.save(msg);

        // ✅ ADDED: fire Kafka event so receiver gets real-time notification
        try {
            chatEventProducer.sendMessageReceivedEvent(
                    saved.getBatchId(),
                    saved.getSenderEmail(),
                    saved.getReceiverEmail(),
                    saved.getMessage()
            );
        } catch (Exception e) {
            System.out.println("Kafka down. Chat saved without event: " + e.getMessage());
        }

        return saved;
    }

    // ================= CONVERSATION — UNCHANGED =================
    public List<ChatMessage> getConversation(Long batchId, String me, String other) {
        boolean allowed =
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(batchId, me, other)
                ||
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(batchId, other, me);
        if (!allowed)
            throw new RuntimeException("Access denied");
        return repository.getConversation(batchId, me, other);
    }

    // ================= TRAINER STUDENTS — UNCHANGED =================
    public List<String> getTrainerStudents(Long batchId, String trainerEmail) {
        return accessRepo.findStudentsOfTrainer(batchId, trainerEmail);
    }

    // ================= UNCHANGED =================
    public String getStudentTrainer(Long batchId, String studentEmail) {
        return accessRepo
                .findTrainerForStudent(batchId, studentEmail)
                .orElseThrow(() ->
                        new RuntimeException("Trainer not assigned to this student"));
    }

    // ================= UNCHANGED =================
    public Map<String, Object> getStudentContext(String studentEmail) {
        ChatClassroomAccess access = accessRepo
                .findStudentIgnoreCase(studentEmail)
                .orElseThrow(() -> new RuntimeException("No classroom assigned"));
        Map<String, Object> res = new HashMap<>();
        res.put("batchId", access.getBatchId());
        res.put("trainerEmail", access.getTrainerEmail());
        return res;
    }
}
