// BatchAssignmentConsumer.java
package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.model.StudentBatchMap;
import com.lms.notification.model.TrainerBatchMap;
import com.lms.notification.repository.StudentBatchMapRepository;
import com.lms.notification.repository.TrainerBatchMapRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final TrainerBatchMapRepository trainerRepo;
    private final StudentBatchMapRepository studentRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
                                   StudentBatchMapRepository studentRepo) {
        this.trainerRepo = trainerRepo;
        this.studentRepo = studentRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-assignment",
                   groupId = "notification-service-group")
    public void consume(String message) {
        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);
            String type  = (String) event.get("type");
            String email = (String) event.get("email");
            Long batchId = ((Number) event.get("batchId")).longValue();

            System.out.println("📥 NOTIF BATCH ASSIGN -> "
                    + type + " | " + email + " | " + batchId);

            switch (type) {
                case "TRAINER_ASSIGNED" ->
                    trainerRepo.save(new TrainerBatchMap(email, batchId));
                case "STUDENT_ASSIGNED" ->
                    studentRepo.save(new StudentBatchMap(email, batchId));
                case "STUDENT_REMOVED" ->
                    studentRepo.deleteByStudentEmailAndBatchId(email, batchId);
                case "TRAINER_REMOVED" -> {
                    trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);
                    studentRepo.deleteByBatchId(batchId);
                }
            }
        } catch (Exception e) {
            System.err.println("BatchAssignmentConsumer error: " + e.getMessage());
        }
    }
}