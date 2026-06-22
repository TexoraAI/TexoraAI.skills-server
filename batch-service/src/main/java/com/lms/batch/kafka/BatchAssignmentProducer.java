//package com.lms.batch.kafka;
//
//import com.lms.batch.event.BatchAssignmentEvent;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class BatchAssignmentProducer {
//
//    private final KafkaTemplate<String, Object> kafkaTemplate;
//
//    public BatchAssignmentProducer(KafkaTemplate<String, Object> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    public void studentAssigned(String email, Long batchId) {
//        send("STUDENT_ASSIGNED", email, batchId, "STUDENT");
//    }
//
//    public void studentRemoved(String email, Long batchId) {
//        send("STUDENT_REMOVED", email, batchId, "STUDENT");
//    }
//
//    public void trainerAssigned(String email, Long batchId) {
//        send("TRAINER_ASSIGNED", email, batchId, "TRAINER");
//    }
//
//    public void trainerRemoved(String email, Long batchId) {
//        send("TRAINER_REMOVED", email, batchId, "TRAINER");
//    }
//
//    private void send(String type, String email, Long batchId, String role) {
//
//        BatchAssignmentEvent event =
//                new BatchAssignmentEvent(type, email, batchId, role);
//
//        kafkaTemplate.send("batch-assignment", event);
//
//        System.out.println("📤 ASSIGNMENT EVENT -> " + type +
//                " | " + email +
//                " | batch=" + batchId);
//    }
//}
package com.lms.batch.kafka;

import com.lms.batch.event.BatchAssignmentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BatchAssignmentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public BatchAssignmentProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // All 4 methods now accept organizationId and pass it into the event

    public void studentAssigned(String email, Long batchId, String organizationId) {
        send("STUDENT_ASSIGNED", email, batchId, "STUDENT", organizationId);
    }

    public void studentRemoved(String email, Long batchId, String organizationId) {
        send("STUDENT_REMOVED", email, batchId, "STUDENT", organizationId);
    }

    public void trainerAssigned(String email, Long batchId, String organizationId) {
        send("TRAINER_ASSIGNED", email, batchId, "TRAINER", organizationId);
    }

    public void trainerRemoved(String email, Long batchId, String organizationId) {
        send("TRAINER_REMOVED", email, batchId, "TRAINER", organizationId);
    }

    private void send(String type, String email, Long batchId,
                      String role, String organizationId) {

        BatchAssignmentEvent event =
                new BatchAssignmentEvent(type, email, batchId, role, organizationId);

        kafkaTemplate.send("batch-assignment", event);

        System.out.println("📤 ASSIGNMENT EVENT -> " + type
                + " | " + email
                + " | batch=" + batchId
                + " | orgId=" + organizationId);
    }
}