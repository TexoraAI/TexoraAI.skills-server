//
//
//
//
//
//package com.lms.video.kafka;
//
//import com.lms.video.model.StudentBatchMap;
//import com.lms.video.model.TrainerBatchMap;
//import com.lms.video.repository.StudentBatchMapRepository;
//import com.lms.video.repository.TrainerBatchMapRepository;
//import com.lms.video.repository.VideoRepository;
//
//import jakarta.transaction.Transactional;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//public class BatchAssignmentConsumer {
//
//    private final TrainerBatchMapRepository trainerRepo;
//    private final StudentBatchMapRepository studentRepo;
//    private final VideoRepository videoRepo;
//
//    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
//                                   StudentBatchMapRepository studentRepo,
//                                   VideoRepository videoRepo) {
//        this.trainerRepo = trainerRepo;
//        this.studentRepo = studentRepo;
//        this.videoRepo = videoRepo;
//    }
//
//    @Transactional
//    @KafkaListener(topics = "batch-assignment", groupId = "video-service-group")
//    public void consume(Map<String, Object> event) {
//
//        String type = (String) event.get("type");
//        String email = (String) event.get("email");
//        Long batchId = ((Number) event.get("batchId")).longValue();
//        String organizationId = (String) event.get("organizationId");
//
//        System.out.println("📥 VIDEO SERVICE EVENT -> " + type + " | " + email + " | " + batchId);
//
//        switch (type) {
//
//            // ================= TRAINER =================
//            case "TRAINER_ASSIGNED" -> {
//                boolean exists = trainerRepo
//                        .findByTrainerEmailAndBatchId(email, batchId)
//                        .isPresent();
//
//                if (!exists) {
//                    trainerRepo.save(new TrainerBatchMap(email, batchId,organizationId));
//                    System.out.println("👨‍🏫 Trainer assigned");
//                } else {
//                    System.out.println("⚠️ Trainer already exists (ignored)");
//                }
//            }
//
//            // ================= STUDENT =================
//            case "STUDENT_ASSIGNED" -> {
//
//                // ✅ Prevent duplicate insert
//                boolean exists = studentRepo
//                        .existsByStudentEmailAndBatchId(email, batchId);
//
//                if (!exists) {
//                    studentRepo.save(new StudentBatchMap(email, batchId,organizationId));
//                    System.out.println("🎓 Student added to batch");
//                } else {
//                    System.out.println("⚠️ Duplicate student assignment ignored");
//                }
//            }
//
//            case "STUDENT_REMOVED" -> {
//                // ✅ Already safe (idempotent)
//                studentRepo.deleteByStudentEmailAndBatchId(email, batchId);
//                System.out.println("🗑 Student removed from batch");
//            }
//
//            // ================= TRAINER REMOVAL =================
//            case "TRAINER_REMOVED" -> {
//
//                // 1. delete trainer mapping
//                trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);
//
//                // 2. remove all students of that batch
//                studentRepo.deleteByBatchId(batchId);
//
//                // 3. delete videos of that batch
//                videoRepo.deleteByBatchId(batchId);
//
//                System.out.println("🧹 FULL TRAINER CLASSROOM CLEANED");
//            }
//        }
//    }
//}

//
package com.lms.video.kafka;
import com.lms.video.model.StudentBatchMap;
import com.lms.video.model.TrainerBatchMap;
import com.lms.video.repository.StudentBatchMapRepository;
import com.lms.video.repository.TrainerBatchMapRepository;
import com.lms.video.repository.VideoRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;
@Service
public class BatchAssignmentConsumer {
    private final TrainerBatchMapRepository trainerRepo;
    private final StudentBatchMapRepository studentRepo;
    private final VideoRepository videoRepo;
    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
                                   StudentBatchMapRepository studentRepo,
                                   VideoRepository videoRepo) {
        this.trainerRepo = trainerRepo;
        this.studentRepo = studentRepo;
        this.videoRepo = videoRepo;
    }
    @Transactional
    @KafkaListener(topics = "batch-assignment", groupId = "video-service-group")
    public void consume(Map<String, Object> event) {
        String type = (String) event.get("type");
        String email = (String) event.get("email");
        Long batchId = ((Number) event.get("batchId")).longValue();
        // ✅ NEW — this field was already on the wire from Batch Service but
        // was previously never read here, so it was silently dropped.
        String organizationId = (String) event.get("organizationId");
        System.out.println("📥 VIDEO SERVICE EVENT -> " + type + " | " + email + " | " + batchId + " | org=" + organizationId);
        switch (type) {
            // ================= TRAINER =================
            case "TRAINER_ASSIGNED" -> {
                boolean exists = trainerRepo
                        .findByTrainerEmailAndBatchId(email, batchId)
                        .isPresent();
                if (!exists) {
                    trainerRepo.save(new TrainerBatchMap(email, batchId, organizationId));   // ✅ NEW — org now stored
                    System.out.println("👨‍🏫 Trainer assigned");
                } else {
                    System.out.println("⚠️ Trainer already exists (ignored)");
                }
            }
            // ================= STUDENT =================
            case "STUDENT_ASSIGNED" -> {
                // ✅ Prevent duplicate insert
                boolean exists = studentRepo
                        .existsByStudentEmailAndBatchId(email, batchId);
                if (!exists) {
                    studentRepo.save(new StudentBatchMap(email, batchId, organizationId));   // ✅ NEW — org now stored
                    System.out.println("🎓 Student added to batch");
                } else {
                    System.out.println("⚠️ Duplicate student assignment ignored");
                }
            }
            case "STUDENT_REMOVED" -> {
                // ✅ Already safe (idempotent)
                studentRepo.deleteByStudentEmailAndBatchId(email, batchId);
                System.out.println("🗑 Student removed from batch");
            }
            // ================= TRAINER REMOVAL =================
            case "TRAINER_REMOVED" -> {
                // 1. delete trainer mapping
                trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);
                // 2. remove all students of that batch
                studentRepo.deleteByBatchId(batchId);
                // 3. delete videos of that batch
                videoRepo.deleteByBatchId(batchId);
                System.out.println("🧹 FULL TRAINER CLASSROOM CLEANED");
            }
        }
    }
}