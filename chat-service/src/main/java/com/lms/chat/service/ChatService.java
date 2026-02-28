
package com.lms.chat.service;
import java.util.HashMap;
import java.util.Map;
import com.lms.chat.entity.ChatClassroomAccess;
import com.lms.chat.entity.ChatMessage;
import com.lms.chat.repository.ChatClassroomAccessRepository;
import com.lms.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
//
//@Service
//public class ChatService {
//
//    private final ChatMessageRepository repository;
//    private final ChatClassroomAccessRepository accessRepo;
//
//    public ChatService(ChatMessageRepository repository,ChatClassroomAccessRepository accessRepo) {
//        this.repository = repository;
//        this.accessRepo=accessRepo;
//    }
//    public ChatMessage send(ChatMessage message) {
//
//        if (message.getBatchId() == null ||
//            message.getSenderEmail() == null ||
//            message.getReceiverEmail() == null) {
//
//            throw new RuntimeException("Invalid chat data");
//        }
//
//        boolean allowed =
//                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(
//                        message.getBatchId(),
//                        message.getReceiverEmail(),  // trainer
//                        message.getSenderEmail()     // student
//                )
//             ||
//                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(
//                        message.getBatchId(),
//                        message.getSenderEmail(),    // trainer
//                        message.getReceiverEmail()   // student
//                );
//
//        if (!allowed)
//            throw new RuntimeException("You are not assigned to this trainer");
//
//        return repository.save(message);
//    }
//
//   
//
// // ================= GET CONVERSATION =================
//    public List<ChatMessage> getConversation(
//            Long batchId,
//            String user1,
//            String user2) {
//
//        return repository.getConversation(batchId, user1, user2);
//    }
//
//    // ================= TRAINER INBOX =================
//    public List<ChatMessage> getTrainerInbox(
//            Long batchId,
//            String trainerEmail) {
//
//        return repository.getTrainerInbox(batchId, trainerEmail);
//    }
//
//    
//}
@Service
public class ChatService {

    private final ChatMessageRepository repository;
    private final ChatClassroomAccessRepository accessRepo;

    public ChatService(ChatMessageRepository repository,
                       ChatClassroomAccessRepository accessRepo) {
        this.repository = repository;
        this.accessRepo = accessRepo;
    }

//    
//    public ChatMessage send(ChatMessage msg, String loggedUser) {
//
//        // ALWAYS trust JWT, not frontend
//        msg.setSenderEmail(loggedUser);
//
//        String otherUser =
//                loggedUser.equalsIgnoreCase(msg.getReceiverEmail())
//                        ? msg.getSenderEmail()
//                        : msg.getReceiverEmail();
//
//        boolean allowed =
//                accessRepo
//                    .existsByBatchIdAndTrainerEmailAndStudentEmail(
//                            msg.getBatchId(),
//                            otherUser,     // trainer
//                            loggedUser     // student
//                    ) ||
//                accessRepo
//                    .existsByBatchIdAndTrainerEmailAndStudentEmail(
//                            msg.getBatchId(),
//                            loggedUser,    // trainer
//                            otherUser      // student
//                    );
//
//
//        if (!allowed) {
//            throw new RuntimeException("You are not assigned to this classroom");
//        }
//
//        msg.setSentAt(LocalDateTime.now());
//        return repository.save(msg);
//    }

    public ChatMessage send(ChatMessage msg, String loggedUser) {

        // always trust JWT
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
        return repository.save(msg);
    }

    
    // ================= CONVERSATION =================
    public List<ChatMessage> getConversation(Long batchId, String me, String other) {

        boolean allowed =
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(batchId, me, other)
                ||
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(batchId, other, me);

        if (!allowed)
            throw new RuntimeException("Access denied");

        return repository.getConversation(batchId, me, other);
    }

    // ================= TRAINER STUDENTS =================
    public List<String> getTrainerStudents(Long batchId, String trainerEmail) {
        return accessRepo.findStudentsOfTrainer(batchId, trainerEmail);
    }
    
    public String getStudentTrainer(Long batchId, String studentEmail) {

        return accessRepo
                .findTrainerForStudent(batchId, studentEmail)
                .orElseThrow(() ->
                        new RuntimeException("Trainer not assigned to this student"));
    }
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
