package com.lms.progress.kafka;

import com.lms.progress.repository.ProgressRepository;
import com.lms.progress.repository.StudentBatchMapRepository;
import com.lms.progress.repository.TrainerBatchMapRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class BatchLifecycleConsumer {

    private final StudentBatchMapRepository studentRepo;
    private final TrainerBatchMapRepository trainerRepo;
    private final ProgressRepository progressRepo;

    public BatchLifecycleConsumer(
            StudentBatchMapRepository studentRepo,
            TrainerBatchMapRepository trainerRepo,
            ProgressRepository progressRepo) {
        this.studentRepo = studentRepo;
        this.trainerRepo = trainerRepo;
        this.progressRepo = progressRepo;
    }

    @Transactional
    @KafkaListener(
        topics = "batch-lifecycle",
        groupId = "progress-service-group"
    )
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");
        System.out.println("📥 PROGRESS LIFECYCLE EVENT -> " + type);

        switch (type) {

            case "BATCH_DELETED" -> {
                Long batchId = ((Number) event.get("batchId")).longValue();

                // 1️⃣ Delete progress for all students in this batch
                studentRepo.findByBatchId(batchId).forEach(map -> {
                    progressRepo.deleteByStudentEmail(map.getStudentEmail());
                    System.out.println("🗑️ Progress deleted for student -> "
                            + map.getStudentEmail());
                });

                // 2️⃣ Delete student batch mappings
                studentRepo.deleteByBatchId(batchId);
                System.out.println("🗑️ StudentBatchMap deleted for batch=" + batchId);

                // 3️⃣ Delete trainer batch mappings
                // (no progress to delete for trainer — just mapping cleanup)
                trainerRepo.deleteByBatchId(batchId);
                System.out.println("🗑️ TrainerBatchMap deleted for batch=" + batchId);
            }

            case "STUDENT_DELETED" -> {
                String email = (String) event.get("email");
                progressRepo.deleteByStudentEmail(email);
                System.out.println("🗑️ Progress deleted for student -> " + email);
            }

            case "TRAINER_DELETED" -> {
                String email = (String) event.get("email");
                // No progress to delete for trainer
                // Just clean up their batch mappings
                trainerRepo.deleteByTrainerEmail(email);
                System.out.println("🗑️ TrainerBatchMap deleted for trainer -> " + email);
            }

            default -> System.out.println("⏭️ Ignored lifecycle event: " + type);
        }
    }
}