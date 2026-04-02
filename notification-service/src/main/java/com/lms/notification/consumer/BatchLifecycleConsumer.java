// BatchLifecycleConsumer.java
package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.repository.StudentBatchMapRepository;
import com.lms.notification.repository.TrainerBatchMapRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class BatchLifecycleConsumer {

    private final StudentBatchMapRepository studentRepo;
    private final TrainerBatchMapRepository trainerRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public BatchLifecycleConsumer(StudentBatchMapRepository studentRepo,
                                  TrainerBatchMapRepository trainerRepo) {
        this.studentRepo = studentRepo;
        this.trainerRepo = trainerRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-lifecycle",
                   groupId = "notification-service-group")
    public void consume(String message) {
        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);
            String type = (String) event.get("type");

            if ("BATCH_DELETED".equals(type)) {
                Long batchId = ((Number) event.get("batchId")).longValue();
                studentRepo.deleteByBatchId(batchId);
                trainerRepo.deleteByBatchId(batchId);
                System.out.println("🔥 NOTIF BATCH CLEANUP -> " + batchId);
            }
        } catch (Exception e) {
            System.err.println("BatchLifecycleConsumer error: " + e.getMessage());
        }
    }
}