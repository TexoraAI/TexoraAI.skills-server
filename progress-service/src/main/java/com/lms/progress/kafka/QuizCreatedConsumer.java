//package com.lms.progress.kafka;
//
//import com.lms.progress.model.QuizProgress;
//import com.lms.progress.repository.QuizProgressRepository;
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
//public class QuizCreatedConsumer {
//
//    private final QuizProgressRepository repo;
//    private final StudentBatchMapRepository studentBatchRepo;
//
//    public QuizCreatedConsumer(QuizProgressRepository repo,
//                               StudentBatchMapRepository studentBatchRepo) {
//        this.repo = repo;
//        this.studentBatchRepo = studentBatchRepo;
//    }
//
//    @KafkaListener(
//            topics = "assessment-events",
//            groupId = "quiz-progress-group"   // ✅ IMPORTANT (unique group)
//    )
//    public void consume(ConsumerRecord<String, Object> record) {
//
//        try {
//            Object value = record.value();
//
//            System.out.println("🔥 QUIZ RAW EVENT: " + value);
//
//            Map<String, Object> event = (Map<String, Object>) value;
//
//            String type = (String) event.get("type");
//            if (!"QUIZ_CREATED".equals(type)) return;
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
//                QuizProgress p = repo
//                        .findByStudentEmailAndBatchId(email, batchId)
//                        .orElseGet(() -> {
//                            QuizProgress fresh = new QuizProgress();
//                            fresh.setStudentEmail(email);
//                            fresh.setBatchId(batchId);
//                            fresh.setCompletedQuizIds(new ArrayList<>());
//                            fresh.setTotalQuizzes(0);
//                            fresh.setPercentage(0);
//                            fresh.setUpdatedAt(Instant.now());
//                            return fresh;
//                        });
//
//                // increment total
//                p.setTotalQuizzes(p.getTotalQuizzes() + 1);
//
//                int completed = p.getCompletedQuizIds().size();
//
//                double pct = p.getTotalQuizzes() > 0
//                        ? (double) completed / p.getTotalQuizzes() * 100
//                        : 0;
//
//                p.setPercentage(Math.min(pct, 100));
//                p.setUpdatedAt(Instant.now());
//
//                repo.save(p);
//            }
//
//            System.out.println("📝 QUIZ_CREATED → batchId=" + batchId);
//
//        } catch (Exception e) {
//            System.err.println("❌ Quiz consumer error: " + e.getMessage());
//        }
//    }
//}

package com.lms.progress.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.progress.model.QuizProgress;
import com.lms.progress.repository.QuizProgressRepository;
import com.lms.progress.repository.StudentBatchMapRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class QuizCreatedConsumer {

    private final QuizProgressRepository repo;
    private final StudentBatchMapRepository studentBatchRepo;
    private final ObjectMapper mapper = new ObjectMapper(); // ✅ ADD

    public QuizCreatedConsumer(QuizProgressRepository repo,
                               StudentBatchMapRepository studentBatchRepo) {
        this.repo = repo;
        this.studentBatchRepo = studentBatchRepo;
    }

    @KafkaListener(
            topics = "assessment-events",
            groupId = "quiz-progress-group"
    )
    public void consume(ConsumerRecord<String, Object> record) {

        try {
            String json = (String) record.value();   // ✅ FIX

            System.out.println("🔥 QUIZ RAW EVENT: " + json);

            Map<String, Object> event = mapper.readValue(json, Map.class); // ✅ FIX

            String type = (String) event.get("type");
            if (!"QUIZ_CREATED".equals(type)) return;

            Map<String, Object> payload =
                    (Map<String, Object>) event.get("payload");

            Long batchId = Long.valueOf(payload.get("batchId").toString());

            List<String> students = studentBatchRepo
                    .findByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();

            for (String email : students) {

                QuizProgress p = repo
                        .findByStudentEmailAndBatchId(email, batchId)
                        .orElseGet(() -> {
                            QuizProgress fresh = new QuizProgress();
                            fresh.setStudentEmail(email);
                            fresh.setBatchId(batchId);
                            fresh.setCompletedQuizIds(new ArrayList<>());
                            fresh.setTotalQuizzes(0);
                            fresh.setPercentage(0);
                            fresh.setUpdatedAt(Instant.now());
                            return fresh;
                        });

                p.setTotalQuizzes(p.getTotalQuizzes() + 1);

                int completed = p.getCompletedQuizIds().size();

                double pct = p.getTotalQuizzes() > 0
                        ? (double) completed / p.getTotalQuizzes() * 100
                        : 0;

                p.setPercentage(Math.min(pct, 100));
                p.setUpdatedAt(Instant.now());

                repo.save(p);
            }

            System.out.println("📝 QUIZ_CREATED → batchId=" + batchId);

        } catch (Exception e) {
            System.err.println("❌ Quiz consumer error: " + e.getMessage());
        }
    }
}