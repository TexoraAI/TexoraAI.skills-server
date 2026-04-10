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
public class VideoEventConsumer {

    private final ProgressRepository progressRepo;
    private final StudentBatchMapRepository studentBatchRepo;

    public VideoEventConsumer(ProgressRepository progressRepo,
                              StudentBatchMapRepository studentBatchRepo) {
        this.progressRepo  = progressRepo;
        this.studentBatchRepo = studentBatchRepo;
    }

    @KafkaListener(topics = "video-uploaded", groupId = "progress-service-group")
    public void onVideoUploaded(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");
            if (!"VIDEO_UPLOADED".equals(type)) return;

            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
            String title  = (String) payload.get("title");
            Long batchId  = Long.valueOf(payload.get("batchId").toString());

            // ✅ Same as CourseEventConsumer — get all students in batch
            List<String> studentEmails = studentBatchRepo
                    .findByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();

            if (studentEmails.isEmpty()) {
                System.out.println("⚠️ No students for batchId=" + batchId);
                return;
            }

            // ✅ Same as ContentEventConsumer — increment totalContentCount
            for (String email : studentEmails) {
                progressRepo.findAll()
                        .stream()
                        .filter(p -> p.getStudentEmail().equals(email))
                        .forEach(p -> {
                            p.setTotalContentCount(p.getTotalContentCount() + 1);

                            int completed = p.getCompletedContentIds() != null
                                    ? p.getCompletedContentIds().size() : 0;

                            double pct = p.getTotalContentCount() > 0
                                    ? (double) completed / p.getTotalContentCount() * 100 : 0;

                            p.setProgressPercentage(Math.min(pct, 100.0));
                            p.setUpdatedAt(Instant.now());
                            progressRepo.save(p);
                        });
            }

            System.out.println("🎥 VIDEO_UPLOADED → totalContentCount +1 | title="
                    + title + " | batchId=" + batchId);

        } catch (Exception e) {
            System.err.println("❌ VideoEventConsumer error: " + e.getMessage());
        }
    }
}