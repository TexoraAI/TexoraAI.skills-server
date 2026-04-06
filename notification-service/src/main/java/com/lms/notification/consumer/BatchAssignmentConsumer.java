//// BatchAssignmentConsumer.java
//package com.lms.notification.consumer;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lms.notification.model.StudentBatchMap;
//import com.lms.notification.model.TrainerBatchMap;
//import com.lms.notification.repository.StudentBatchMapRepository;
//import com.lms.notification.repository.TrainerBatchMapRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//import java.util.Map;
//
//@Service
//public class BatchAssignmentConsumer {
//
//    private final TrainerBatchMapRepository trainerRepo;
//    private final StudentBatchMapRepository studentRepo;
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
//                                   StudentBatchMapRepository studentRepo) {
//        this.trainerRepo = trainerRepo;
//        this.studentRepo = studentRepo;
//    }
//
//    @Transactional
//    @KafkaListener(topics = "batch-assignment",
//                   groupId = "notification-service-group")
//    public void consume(String message) {
//        try {
//            Map<String, Object> event = mapper.readValue(message, Map.class);
//            String type  = (String) event.get("type");
//            String email = (String) event.get("email");
//            Long batchId = ((Number) event.get("batchId")).longValue();
//
//            System.out.println("📥 NOTIF BATCH ASSIGN -> "
//                    + type + " | " + email + " | " + batchId);
//
//            switch (type) {
//                case "TRAINER_ASSIGNED" ->
//                    trainerRepo.save(new TrainerBatchMap(email, batchId));
//                case "STUDENT_ASSIGNED" ->
//                    studentRepo.save(new StudentBatchMap(email, batchId));
//                case "STUDENT_REMOVED" ->
//                    studentRepo.deleteByStudentEmailAndBatchId(email, batchId);
//                case "TRAINER_REMOVED" -> {
//                    trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);
//                    studentRepo.deleteByBatchId(batchId);
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("BatchAssignmentConsumer error: " + e.getMessage());
//        }
//    }
//}
package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.model.Notification;
import com.lms.notification.model.StudentBatchMap;
import com.lms.notification.model.TrainerBatchMap;
import com.lms.notification.repository.NotificationRepository;
import com.lms.notification.repository.StudentBatchMapRepository;
import com.lms.notification.repository.TrainerBatchMapRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final TrainerBatchMapRepository trainerRepo;
    private final StudentBatchMapRepository studentRepo;
    private final NotificationRepository notificationRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
                                   StudentBatchMapRepository studentRepo,
                                   NotificationRepository notificationRepo,
                                   SimpMessagingTemplate messagingTemplate) {
        this.trainerRepo = trainerRepo;
        this.studentRepo = studentRepo;
        this.notificationRepo = notificationRepo;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    @KafkaListener(topics = "batch-assignment", groupId = "notification-service-group")
    public void consume(String message) {
        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);
            String type    = (String) event.get("type");
            String email   = (String) event.get("email");
            Long   batchId = ((Number) event.get("batchId")).longValue();

            System.out.println("📥 NOTIF BATCH ASSIGN -> " + type + " | " + email + " | " + batchId);

            switch (type) {

                case "TRAINER_ASSIGNED" -> {
                    trainerRepo.save(new TrainerBatchMap(email, batchId));
                    saveAndPush(
                        email, "TRAINER",
                        "BATCH_ASSIGNED",
                        "New Batch Assigned 🏫",
                        "You have been assigned to batch #" + batchId
                    );
                }

                case "STUDENT_ASSIGNED" -> {
                    studentRepo.save(new StudentBatchMap(email, batchId));
                    saveAndPush(
                        email, "STUDENT",
                        "BATCH_ASSIGNED",
                        "New Batch Assigned 🎓",
                        "You have been added to batch #" + batchId
                    );
                }

                case "STUDENT_REMOVED" -> {
                    studentRepo.deleteByStudentEmailAndBatchId(email, batchId);
                    saveAndPush(
                        email, "STUDENT",
                        "BATCH_UPDATE",
                        "Removed from Batch",
                        "You have been removed from batch #" + batchId
                    );
                }

                case "TRAINER_REMOVED" -> {
                    trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);
                    studentRepo.deleteByBatchId(batchId);
                    saveAndPush(
                        email, "TRAINER",
                        "BATCH_UPDATE",
                        "Removed from Batch",
                        "You have been unassigned from batch #" + batchId
                    );
                }
            }

        } catch (Exception e) {
            System.err.println("BatchAssignmentConsumer error: " + e.getMessage());
        }
    }

    // ── saves to DB + pushes live via WebSocket ──────────────
    private void saveAndPush(String email, String role,
                              String type, String title, String message) {
        Notification n = new Notification();
        n.setUserId(email);
        n.setUserRole(role);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRead(false);
        

        notificationRepo.save(n);  // ← persists to DB (survives re-login)

        // ← pushes live to open browser tab instantly
        messagingTemplate.convertAndSend(
            "/topic/notifications/user/" + email, n
        );

        System.out.println("✅ Notification saved + pushed to: " + email);
    }
}