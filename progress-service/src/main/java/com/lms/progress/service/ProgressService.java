package com.lms.progress.service;

import com.lms.progress.dto.ProgressRequest;
import com.lms.progress.dto.ProgressResponse;
import com.lms.progress.model.Progress;
import com.lms.progress.repository.ProgressRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProgressService {

    private final ProgressRepository    repo;
    private final KafkaProducerService  producer;

    public ProgressService(ProgressRepository repo, KafkaProducerService producer) {
        this.repo     = repo;
        this.producer = producer;
    }

    // ============================
    // CREATE
    // ============================
    public ProgressResponse create(ProgressRequest req) {
        Progress p = new Progress();
        p.setStudentEmail(req.getStudentEmail());
        p.setCourseId(req.getCourseId());
        p.setCompletedContentIds(
            req.getCompletedContentIds() != null ? req.getCompletedContentIds() : new ArrayList<>()
        );
        p.setTotalContentCount(req.getTotalContentCount());
        p.setProgressPercentage(req.getProgressPercentage());
        p.setUpdatedAt(Instant.now());

        Progress saved = repo.save(p);
        fireCompletionEventIfNeeded(saved, 0);
        return toResponse(saved);
    }

    // ============================
    // GET BY ID
    // ============================
    public ProgressResponse getById(Long id) {
        Progress p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progress not found: " + id));
        return toResponse(p);
    }

    // ============================
    // GET BY EMAIL + COURSE
    // ============================
    public ProgressResponse getByUserAndCourse(String email, Long courseId) {
        Progress p = repo.findByStudentEmailAndCourseId(email, courseId)
                .orElseThrow(() -> new RuntimeException("Progress not found"));
        return toResponse(p);
    }

    // ============================
    // UPDATE
    // ============================
    public ProgressResponse update(Long id, ProgressRequest req) {
        Progress p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progress not found"));

        double before = p.getProgressPercentage();

        if (req.getStudentEmail()        != null) p.setStudentEmail(req.getStudentEmail());
        if (req.getCourseId()            != null) p.setCourseId(req.getCourseId());
        if (req.getCompletedContentIds() != null) p.setCompletedContentIds(req.getCompletedContentIds());
        if (req.getTotalContentCount()    > 0)    p.setTotalContentCount(req.getTotalContentCount());

        p.setProgressPercentage(req.getProgressPercentage());
        p.setUpdatedAt(Instant.now());

        Progress saved = repo.save(p);
        fireCompletionEventIfNeeded(saved, before);
        return toResponse(saved);
    }

    // ============================
    // MARK CONTENT AS COMPLETE
    // Called by REST endpoint when student opens video/PDF
    // ============================
    public ProgressResponse markContentComplete(String email,
                                                Long   courseId,
                                                Long   contentId,
                                                int    totalContentCount) {

        // Find existing OR create fresh record
        Progress p = repo.findByStudentEmailAndCourseId(email, courseId)
                .orElseGet(() -> {
                    Progress fresh = new Progress();
                    fresh.setStudentEmail(email);
                    fresh.setCourseId(courseId);
                    fresh.setCompletedContentIds(new ArrayList<>());
                    fresh.setTotalContentCount(totalContentCount);
                    fresh.setProgressPercentage(0);
                    fresh.setUpdatedAt(Instant.now());
                    return fresh;
                });

        double before = p.getProgressPercentage();

        // Always sync latest total in case new content was added to course
        p.setTotalContentCount(totalContentCount);

        List<Long> completed = p.getCompletedContentIds();
        if (completed == null) completed = new ArrayList<>();

        // Add only if not already marked done
        if (!completed.contains(contentId)) {
            completed.add(contentId);
        }
        p.setCompletedContentIds(completed);

        // Accurate percentage
        double percentage = totalContentCount > 0
                ? (double) completed.size() / totalContentCount * 100
                : 0;
        percentage = Math.min(percentage, 100.0);

        p.setProgressPercentage(percentage);
        p.setUpdatedAt(Instant.now());

        Progress saved = repo.save(p);

        System.out.println("✅ markContentComplete → " + email
                + " | course=" + courseId
                + " | content=" + contentId
                + " | " + completed.size() + "/" + totalContentCount
                + " = " + String.format("%.1f", percentage) + "%");

        fireCompletionEventIfNeeded(saved, before);
        return toResponse(saved);
    }

    // ============================
    // DELETE BY EMAIL
    // ============================
    public void deleteByEmail(String email) {
        repo.deleteByStudentEmail(email);
        producer.sendProgressEvent("Progress deleted for user: " + email);
    }

    // ============================
    // DELETE BY ID
    // ============================
    public void deleteById(Long id) {
        repo.deleteById(id);
        producer.sendProgressEvent("Progress deleted for ID: " + id);
    }

    // ============================
    // PRIVATE — fire Kafka only on course completion
    // ============================
    private void fireCompletionEventIfNeeded(Progress p, double before) {
        if (before < 100 && p.getProgressPercentage() >= 100) {
            producer.sendProgressEvent(
                "User " + p.getStudentEmail() +
                " completed course " + p.getCourseId()
            );
        }
    }

    // ============================
    // MAPPER
    // ============================
    private ProgressResponse toResponse(Progress p) {
        ProgressResponse r = new ProgressResponse();
        r.setProgressId(p.getId());
        r.setStudentEmail(p.getStudentEmail());
        r.setCourseId(p.getCourseId());
        r.setCompletedContentIds(p.getCompletedContentIds());
        r.setTotalContentCount(p.getTotalContentCount());
        r.setProgressPercentage(p.getProgressPercentage());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}