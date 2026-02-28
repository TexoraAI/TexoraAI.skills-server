package com.lms.batch.kafka;

import com.lms.batch.event.BatchLifecycleEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BatchLifecycleProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BatchLifecycleProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void batchDeleted(Long batchId) {
        send("BATCH_DELETED", batchId, null, null);
    }

    public void branchDeleted(Long branchId) {
        send("BRANCH_DELETED", null, branchId, null);
    }

    public void studentDeleted(String email) {
        send("STUDENT_DELETED", null, null, email);
    }

    public void trainerDeleted(String email) {
        send("TRAINER_DELETED", null, null, email);
    }

    private void send(String type, Long batchId, Long branchId, String email) {

        BatchLifecycleEvent event =
                new BatchLifecycleEvent(type, batchId, branchId, email);

        kafkaTemplate.send("batch-lifecycle", event);

        System.out.println("🔥 LIFECYCLE EVENT -> " + type +
                " | batch=" + batchId +
                " | branch=" + branchId +
                " | email=" + email);
    }
}
