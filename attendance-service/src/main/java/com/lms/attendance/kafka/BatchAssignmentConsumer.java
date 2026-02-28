package com.lms.attendance.kafka;

import com.lms.attendance.entity.StudentBatchAccess;
import com.lms.attendance.entity.TrainerBatchAccess;
import com.lms.attendance.repository.StudentBatchAccessRepository;
import com.lms.attendance.repository.TrainerBatchAccessRepository;
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

        System.out.println("📥 ATTENDANCE EVENT -> " + type + " | " + email + " | batch=" + batchId);

        switch (type) {

            // ================= TRAINER =================
            case "TRAINER_ASSIGNED" -> {
                trainerRepo.save(new TrainerBatchAccess(batchId, email));
                System.out.println("👨‍🏫 Trainer access granted");
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

                studentRepo.save(new StudentBatchAccess(batchId, userId, email));
                System.out.println("🎓 Student added to attendance list");
            }

            case "STUDENT_REMOVED" -> {
                studentRepo.deleteByBatchIdAndStudentEmail(batchId, email);
                System.out.println("🗑 Student removed from attendance list");
            }
        }
    }
}
