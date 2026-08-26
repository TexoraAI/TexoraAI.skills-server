//
//package com.lms.attendance.kafka;
//
//import com.lms.attendance.entity.StudentBatchAccess;
//import com.lms.attendance.entity.TrainerBatchAccess;
//import com.lms.attendance.repository.StudentBatchAccessRepository;
//import com.lms.attendance.repository.TrainerBatchAccessRepository;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Map;
//
//@Service
//public class BatchAssignmentConsumer {
//
//    private final TrainerBatchAccessRepository trainerRepo;
//    private final StudentBatchAccessRepository studentRepo;
//
//    public BatchAssignmentConsumer(TrainerBatchAccessRepository trainerRepo,
//                                   StudentBatchAccessRepository studentRepo) {
//        this.trainerRepo = trainerRepo;
//        this.studentRepo = studentRepo;
//    }
//
//    @Transactional
//    @KafkaListener(topics = "batch-assignment", groupId = "attendance-service-group")
//    public void consume(Map<String, Object> event) {
//
//        String type = (String) event.get("type");
//        String email = (String) event.get("email");
//        Long batchId = ((Number) event.get("batchId")).longValue();
//
//        // NEW — trusted organizationId coming from Batch Service via Kafka; nullable for standalone users
//        String organizationId = (String) event.get("organizationId");
//
//        System.out.println("📥 ATTENDANCE EVENT -> " + type + " | " + email + " | batch=" + batchId + " | orgId=" + organizationId);
//
//        switch (type) {
//
//            // ================= TRAINER =================
//            case "TRAINER_ASSIGNED" -> {
//                trainerRepo.save(new TrainerBatchAccess(batchId, email, organizationId));
//                System.out.println("👨‍🏫 Trainer access granted");
//            }
//
//            case "TRAINER_REMOVED" -> {
//                trainerRepo.deleteByBatchIdAndTrainerEmail(batchId, email);
//                System.out.println("🚫 Trainer access removed");
//            }
//
//            // ================= STUDENT =================
//            case "STUDENT_ASSIGNED" -> {
//                Long userId = event.get("userId") == null
//                        ? null
//                        : ((Number) event.get("userId")).longValue();
//
//                studentRepo.save(new StudentBatchAccess(batchId, userId, email, organizationId));
//                System.out.println("🎓 Student added to attendance list");
//            }
//
//            case "STUDENT_REMOVED" -> {
//                studentRepo.deleteByBatchIdAndStudentEmail(batchId, email);
//                System.out.println("🗑 Student removed from attendance list");
//            }
//        }
//    }
//}
package com.lms.attendance.kafka;

import com.lms.attendance.entity.StudentBatchAccess;
import com.lms.attendance.entity.TrainerBatchAccess;
import com.lms.attendance.repository.StudentBatchAccessRepository;
import com.lms.attendance.repository.TrainerBatchAccessRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final TrainerBatchAccessRepository trainerRepo;
    private final StudentBatchAccessRepository studentRepo;

    public BatchAssignmentConsumer(TrainerBatchAccessRepository trainerRepo,
                                   StudentBatchAccessRepository studentRepo) {
        this.trainerRepo = trainerRepo;
        this.studentRepo = studentRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-assignment", groupId = "attendance-service-group")
    public void consume(Map<String, Object> event) {
        String type = (String) event.get("type");
        String email = (String) event.get("email");
        Long batchId = ((Number) event.get("batchId")).longValue();
        // NEW — trusted organizationId coming from Batch Service via Kafka; nullable for standalone users
        String organizationId = (String) event.get("organizationId");

        System.out.println("📥 ATTENDANCE EVENT -> " + type + " | " + email + " | batch=" + batchId + " | orgId=" + organizationId);

        try {
            switch (type) {
                // ================= TRAINER =================
                case "TRAINER_ASSIGNED" -> {
                    if (!trainerRepo.existsByBatchIdAndTrainerEmail(batchId, email)) {
                        trainerRepo.save(new TrainerBatchAccess(batchId, email, organizationId));
                        System.out.println("👨‍🏫 Trainer access granted");
                    } else {
                        System.out.println("↪️ Trainer access already exists — skipping duplicate (idempotent replay)");
                    }
                }
                case "TRAINER_REMOVED" -> {
                    trainerRepo.deleteByBatchIdAndTrainerEmail(batchId, email);
                    System.out.println("🚫 Trainer access removed");
                }

                // ================= STUDENT =================
                case "STUDENT_ASSIGNED" -> {
                    Long userId = event.get("userId") == null
                            ? null
                            : ((Number) event.get("userId")).longValue();
                    if (!studentRepo.existsByBatchIdAndStudentEmail(batchId, email)) {
                        studentRepo.save(new StudentBatchAccess(batchId, userId, email, organizationId));
                        System.out.println("🎓 Student added to attendance list");
                    } else {
                        System.out.println("↪️ Student access already exists — skipping duplicate (idempotent replay)");
                    }
                }
                case "STUDENT_REMOVED" -> {
                    studentRepo.deleteByBatchIdAndStudentEmail(batchId, email);
                    System.out.println("🗑 Student removed from attendance list");
                }
            }
        } catch (DataIntegrityViolationException e) {
            // Defense-in-depth: even with the exists-checks above, a race between
            // two near-simultaneous redeliveries of the same message could both
            // pass the check before either commits. The DB unique constraint is
            // the final backstop — swallow it here so the offset still commits
            // and Kafka doesn't retry a message that already succeeded logically.
            System.out.println("⚠️ Duplicate batch-assignment event ignored (race on unique constraint): " + e.getMessage());
        }
    }
}