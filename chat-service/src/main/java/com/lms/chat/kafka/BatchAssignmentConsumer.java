//package com.lms.chat.kafka;
//
//import com.lms.chat.entity.ChatClassroomAccess;
//import com.lms.chat.repository.ChatClassroomAccessRepository;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//import jakarta.transaction.Transactional;
//import java.util.Map;
//
//@Service
//public class BatchAssignmentConsumer {
//
//    private final ChatClassroomAccessRepository repo;
//
//    public BatchAssignmentConsumer(ChatClassroomAccessRepository repo) {
//        this.repo = repo;
//    }
//    @Transactional
//    @KafkaListener(topics = "batch-assignment", groupId = "chat-service-group")
//    public void consume(Map<String, Object> event) {
//
//        String type = (String) event.get("type");
//        String email = (String) event.get("email");
//        Long batchId = ((Number) event.get("batchId")).longValue();
//
//        System.out.println("📩 CHAT EVENT -> " + type + " | " + email + " | batch=" + batchId);
//
//        switch (type) {
//
//            // student assigned to trainer → allow chat
//            case "STUDENT_ASSIGNED" -> {
//                String trainer = (String) event.get("trainerEmail");
//                repo.save(new ChatClassroomAccess(batchId, trainer, email));
//                System.out.println("✅ CHAT ACCESS GRANTED");
//            }
//
//            // student removed → block chat
//            case "STUDENT_REMOVED" -> {
//                repo.deleteByStudentEmailAndBatchId(email, batchId);
//                System.out.println("🚫 CHAT ACCESS REMOVED (student)");
//            }
//
//            // trainer removed → remove all its students access
//            case "TRAINER_REMOVED" -> {
//                repo.deleteByTrainerEmailAndBatchId(email, batchId);
//                System.out.println("🚫 CHAT ACCESS REMOVED (trainer)");
//            }
//
//            // trainer assigned → nothing
//            case "TRAINER_ASSIGNED" -> {
//                System.out.println("ℹ️ Trainer assigned (no chat access yet)");
//            }
//        }
//    }
//}




package com.lms.chat.kafka;

import com.lms.chat.entity.ChatBatchTrainer;
import com.lms.chat.entity.ChatClassroomAccess;
import com.lms.chat.repository.ChatBatchTrainerRepository;
import com.lms.chat.repository.ChatClassroomAccessRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final ChatClassroomAccessRepository accessRepo;
    private final ChatBatchTrainerRepository trainerRepo;

    public BatchAssignmentConsumer(
            ChatClassroomAccessRepository accessRepo,
            ChatBatchTrainerRepository trainerRepo) {
        this.accessRepo = accessRepo;
        this.trainerRepo = trainerRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-assignment", groupId = "chat-service-group")
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");
        String email = (String) event.get("email");
        Long batchId = ((Number) event.get("batchId")).longValue();

        System.out.println("📩 CHAT EVENT -> " + type + " | " + email + " | batch=" + batchId);

        switch (type) {

            // ================= TRAINER ASSIGNED =================
            case "TRAINER_ASSIGNED" -> {

                // store trainer
                ChatBatchTrainer trainer = new ChatBatchTrainer();
                trainer.setBatchId(batchId);
                trainer.setTrainerEmail(email);
                trainerRepo.save(trainer);

                // update existing students
                accessRepo.attachTrainerToBatch(batchId, email);

                System.out.println("👨‍🏫 Trainer stored & linked to old students");
            }

            // ================= STUDENT ASSIGNED =================
            case "STUDENT_ASSIGNED" -> {

                String trainerEmail = trainerRepo
                        .findByBatchId(batchId)
                        .map(ChatBatchTrainer::getTrainerEmail)
                        .orElse(null);

                accessRepo.save(new ChatClassroomAccess(batchId, trainerEmail, email));

                System.out.println("🎓 Student linked to trainer");
            }

            // ================= STUDENT REMOVED =================
            case "STUDENT_REMOVED" -> {
                accessRepo.deleteByStudentEmailAndBatchId(email, batchId);
                System.out.println("🚫 CHAT ACCESS REMOVED (student)");
            }

            // ================= TRAINER REMOVED =================
            case "TRAINER_REMOVED" -> {
                accessRepo.deleteByTrainerEmailAndBatchId(email, batchId);
                trainerRepo.findByBatchId(batchId).ifPresent(trainerRepo::delete);
                System.out.println("🚫 CHAT ACCESS REMOVED (trainer)");
            }
        }
    }
}
