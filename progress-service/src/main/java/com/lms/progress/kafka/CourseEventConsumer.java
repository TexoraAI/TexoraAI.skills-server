

package com.lms.progress.kafka;

import com.lms.progress.model.Progress;
import com.lms.progress.repository.ProgressRepository;
import com.lms.progress.repository.StudentBatchMapRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class CourseEventConsumer {

    private final ProgressRepository progressRepo;
    private final StudentBatchMapRepository studentBatchRepo;

    public CourseEventConsumer(ProgressRepository progressRepo,
                               StudentBatchMapRepository studentBatchRepo) {
        this.progressRepo     = progressRepo;
        this.studentBatchRepo = studentBatchRepo;
    }

    @KafkaListener(topics = "course-events", groupId = "progress-service-group")
    public void onCourseCreated(Map<String, Object> event) {

        try {
            String type = (String) event.get("type");

            if (!"COURSE_CREATED".equals(type)) return;

            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            Long courseId = Long.valueOf(payload.get("courseId").toString());
            Long batchId  = Long.valueOf(payload.get("batchId").toString());

            // 1️⃣ Get all students in this batch
            List<String> studentEmails = studentBatchRepo
                    .findByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();

            if (studentEmails.isEmpty()) {
                System.out.println("⚠️ No students found for batchId=" + batchId);
                return;
            }

            // 2️⃣ Create Progress row per student — skip if already exists
            for (String email : studentEmails) {

                boolean exists = progressRepo
                        .findByStudentEmailAndCourseId(email, courseId)
                        .isPresent();

                if (exists) {
                    System.out.println("⏭️ Progress already exists → " + email);
                    continue;
                }

                Progress p = new Progress();
                p.setStudentEmail(email);
                p.setCourseId(courseId);
                p.setCompletedContentIds(List.of());
                p.setProgressPercentage(0.0);
                p.setUpdatedAt(Instant.now());

                progressRepo.save(p);

                System.out.println("✅ Progress initialized → " + email
                        + " | courseId=" + courseId);
            }

            System.out.println("🎯 COURSE_CREATED done → courseId=" + courseId
                    + " | students=" + studentEmails.size());

        } catch (Exception e) {
            System.err.println("❌ CourseEventConsumer error: " + e.getMessage());
        }
    }
}