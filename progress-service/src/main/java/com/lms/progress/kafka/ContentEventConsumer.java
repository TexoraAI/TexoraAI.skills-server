package com.lms.progress.kafka;

import com.lms.progress.model.Progress;
import com.lms.progress.repository.ProgressRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ContentEventConsumer {

    private final ProgressRepository progressRepo;

    public ContentEventConsumer(ProgressRepository progressRepo) {
        this.progressRepo = progressRepo;
    }

    // ============================
    // Listens to OLD topic — only handles CONTENT_CREATED
    // CONTENT_COMPLETED removed from here (handled by REST endpoint now)
    // ============================
    @KafkaListener(topics = "content-events", groupId = "progress-service-group")
    public void consume(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            if (payload == null) return;

            Long courseId = Long.valueOf(payload.get("courseId").toString());

            // ✅ Only CONTENT_CREATED here — increment totalContentCount
            if ("CONTENT_CREATED".equals(type)) {
                List<Progress> all = progressRepo.findAll();
                for (Progress p : all) {
                    if (p.getCourseId().equals(courseId)) {
                        p.setTotalContentCount(p.getTotalContentCount() + 1);

                        // Recalculate percentage since total changed
                        int completed = p.getCompletedContentIds() != null
                                ? p.getCompletedContentIds().size() : 0;

                        double pct = p.getTotalContentCount() > 0
                                ? (double) completed / p.getTotalContentCount() * 100
                                : 0;

                        p.setProgressPercentage(Math.min(pct, 100.0));
                        p.setUpdatedAt(Instant.now());
                        progressRepo.save(p);
                    }
                }
                System.out.println("📦 CONTENT_CREATED → totalContentCount +1 for courseId=" + courseId);
            }

        } catch (Exception e) {
            System.err.println("❌ ContentEventConsumer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ============================
    // NEW — listens to SEPARATE topic: content-progress-events
    // Only this consumer handles student progress completion
    // Notification service never touches this topic
    // ============================
    @KafkaListener(topics = "content-progress-events", groupId = "progress-service-group")
    public void consumeProgress(Map<String, Object> payload) {
        try {
            String email      = (String) payload.get("studentEmail");
            Long   courseId   = Long.valueOf(payload.get("courseId").toString());
            Long   contentId  = Long.valueOf(payload.get("contentId").toString());
            int    totalCount = Integer.parseInt(payload.get("totalContentCount").toString());

            Progress p = progressRepo
                    .findByStudentEmailAndCourseId(email, courseId)
                    .orElseGet(() -> {
                        Progress fresh = new Progress();
                        fresh.setStudentEmail(email);
                        fresh.setCourseId(courseId);
                        fresh.setCompletedContentIds(new ArrayList<>());
                        fresh.setTotalContentCount(totalCount);
                        fresh.setProgressPercentage(0);
                        fresh.setUpdatedAt(Instant.now());
                        return fresh;
                    });

            p.setTotalContentCount(totalCount);

            List<Long> completed = p.getCompletedContentIds();
            if (completed == null) completed = new ArrayList<>();
            if (!completed.contains(contentId)) completed.add(contentId);
            p.setCompletedContentIds(completed);

            double pct = totalCount > 0
                    ? (double) completed.size() / totalCount * 100 : 0;

            p.setProgressPercentage(Math.min(pct, 100.0));
            p.setUpdatedAt(Instant.now());
            progressRepo.save(p);

            System.out.println("📈 Progress updated → " + email
                    + " | " + completed.size() + "/" + totalCount
                    + " = " + String.format("%.1f", pct) + "%");

        } catch (Exception e) {
            System.err.println("❌ consumeProgress error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}