package com.lms.progress.kafka;

import com.lms.progress.model.StudentBatchMap;
import com.lms.progress.model.TrainerBatchMap;
import com.lms.progress.repository.StudentBatchMapRepository;
import com.lms.progress.repository.TrainerBatchMapRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final StudentBatchMapRepository studentRepo;
    private final TrainerBatchMapRepository trainerRepo;

    public BatchAssignmentConsumer(
            StudentBatchMapRepository studentRepo,
            TrainerBatchMapRepository trainerRepo) {
        this.studentRepo = studentRepo;
        this.trainerRepo = trainerRepo;
    }

    @Transactional
    @KafkaListener(
        topics = "batch-assignment",
        groupId = "progress-service-group"
    )
    public void consume(Map<String, Object> event) {

        String type  = (String) event.get("type");
        String email = (String) event.get("email");
        Long batchId = ((Number) event.get("batchId")).longValue();

        System.out.println("📥 PROGRESS SERVICE ASSIGNMENT EVENT -> "
                + type + " | " + email + " | batch=" + batchId);

        switch (type) {

            case "STUDENT_ASSIGNED" -> {
                if (!studentRepo.existsByStudentEmailAndBatchId(email, batchId)) {
                    studentRepo.save(new StudentBatchMap(email, batchId));
                    System.out.println("✅ Student batch map saved -> "
                            + email + " batch=" + batchId);
                }
            }

            case "STUDENT_REMOVED" -> {
                studentRepo.deleteByStudentEmailAndBatchId(email, batchId);
                System.out.println("🗑️ Student batch map removed -> "
                        + email + " batch=" + batchId);
            }

            case "TRAINER_ASSIGNED" -> {
                if (!trainerRepo.existsByTrainerEmailAndBatchId(email, batchId)) {
                    trainerRepo.save(new TrainerBatchMap(email, batchId));
                    System.out.println("✅ Trainer batch map saved -> "
                            + email + " batch=" + batchId);
                }
            }

            case "TRAINER_REMOVED" -> {
                trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);
                System.out.println("🗑️ Trainer batch map removed -> "
                        + email + " batch=" + batchId);
            }

            default -> System.out.println("⏭️ Ignored event type: " + type);
        }
    }
}