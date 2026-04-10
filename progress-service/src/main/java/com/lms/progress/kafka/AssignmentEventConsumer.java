//package com.lms.progress.kafka;
//
//import com.lms.progress.model.AssignmentProgress;
//import com.lms.progress.repository.AssignmentProgressRepository;
//import com.lms.progress.repository.StudentBatchMapRepository;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class AssignmentEventConsumer {
//
//    private final AssignmentProgressRepository repo;
//    private final StudentBatchMapRepository studentBatchRepo;
//
//    public AssignmentEventConsumer(AssignmentProgressRepository repo,
//                                   StudentBatchMapRepository studentBatchRepo) {
//        this.repo = repo;
//        this.studentBatchRepo = studentBatchRepo;
//    }
//
//    @KafkaListener(
//            topics = "assessment-events",
//            groupId = "assignment-progress-group"   // ✅ IMPORTANT (unique group)
//    )
//    public void consume(ConsumerRecord<String, Object> record) {
//
//        try {
//            Object value = record.value();
//
//            System.out.println("🔥 ASSIGNMENT RAW EVENT: " + value);
//
//            Map<String, Object> event = (Map<String, Object>) value;
//
//            String type = (String) event.get("type");
//            if (!"ASSIGNMENT_CREATED".equals(type)) return;
//
//            Map<String, Object> payload =
//                    (Map<String, Object>) event.get("payload");
//
//            Long batchId = Long.valueOf(payload.get("batchId").toString());
//
//            List<String> students = studentBatchRepo
//                    .findByBatchId(batchId)
//                    .stream()
//                    .map(m -> m.getStudentEmail())
//                    .toList();
//
//            if (students.isEmpty()) {
//                System.out.println("⚠️ No students for batchId=" + batchId);
//                return;
//            }
//
//            for (String email : students) {
//
//                AssignmentProgress p = repo
//                        .findByStudentEmailAndBatchId(email, batchId)
//                        .orElseGet(() -> {
//                            AssignmentProgress fresh = new AssignmentProgress();
//                            fresh.setStudentEmail(email);
//                            fresh.setBatchId(batchId);
//                            fresh.setCompletedAssignmentIds(new ArrayList<>());
//                            fresh.setTotalAssignments(0);
//                            fresh.setPercentage(0);
//                            fresh.setUpdatedAt(Instant.now());
//                            return fresh;
//                        });
//
//                // increment total
//                p.setTotalAssignments(p.getTotalAssignments() + 1);
//
//                int completed = p.getCompletedAssignmentIds().size();
//
//                double pct = p.getTotalAssignments() > 0
//                        ? (double) completed / p.getTotalAssignments() * 100
//                        : 0;
//
//                p.setPercentage(Math.min(pct, 100));
//                p.setUpdatedAt(Instant.now());
//
//                repo.save(p);
//            }
//
//            System.out.println("📘 ASSIGNMENT_CREATED → batchId=" + batchId);
//
//        } catch (Exception e) {
//            System.err.println("❌ Assignment consumer error: " + e.getMessage());
//        }
//    }
//}

package com.lms.progress.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.progress.model.AssignmentProgress;
import com.lms.progress.repository.AssignmentProgressRepository;
import com.lms.progress.repository.StudentBatchMapRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AssignmentEventConsumer {

    private final AssignmentProgressRepository repo;
    private final StudentBatchMapRepository studentBatchRepo;
    private final ObjectMapper mapper = new ObjectMapper(); // ✅ ADD

    public AssignmentEventConsumer(AssignmentProgressRepository repo,
                                   StudentBatchMapRepository studentBatchRepo) {
        this.repo = repo;
        this.studentBatchRepo = studentBatchRepo;
    }

    @KafkaListener(
            topics = "assessment-events",
            groupId = "assignment-progress-group"
    )
    public void consume(ConsumerRecord<String, Object> record) {

        try {
            String json = (String) record.value();   // ✅ FIX

            System.out.println("🔥 ASSIGNMENT RAW EVENT: " + json);

            Map<String, Object> event = mapper.readValue(json, Map.class); // ✅ FIX

            String type = (String) event.get("type");
            if (!"ASSIGNMENT_CREATED".equals(type)) return;

            Map<String, Object> payload =
                    (Map<String, Object>) event.get("payload");

            Long batchId = Long.valueOf(payload.get("batchId").toString());

            List<String> students = studentBatchRepo
                    .findByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();

            for (String email : students) {

                AssignmentProgress p = repo
                        .findByStudentEmailAndBatchId(email, batchId)
                        .orElseGet(() -> {
                            AssignmentProgress fresh = new AssignmentProgress();
                            fresh.setStudentEmail(email);
                            fresh.setBatchId(batchId);
                            fresh.setCompletedAssignmentIds(new ArrayList<>());
                            fresh.setTotalAssignments(0);
                            fresh.setPercentage(0);
                            fresh.setUpdatedAt(Instant.now());
                            return fresh;
                        });

                p.setTotalAssignments(p.getTotalAssignments() + 1);

                int completed = p.getCompletedAssignmentIds().size();

                double pct = p.getTotalAssignments() > 0
                        ? (double) completed / p.getTotalAssignments() * 100
                        : 0;

                p.setPercentage(Math.min(pct, 100));
                p.setUpdatedAt(Instant.now());

                repo.save(p);
            }

            System.out.println("📘 ASSIGNMENT_CREATED → batchId=" + batchId);

        } catch (Exception e) {
            System.err.println("❌ Assignment consumer error: " + e.getMessage());
        }
    }
}