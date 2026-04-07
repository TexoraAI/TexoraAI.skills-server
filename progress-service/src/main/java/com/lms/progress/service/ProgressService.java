//package com.lms.progress.service;
//
//import com.lms.progress.dto.ProgressRequest;
//import com.lms.progress.dto.ProgressResponse;
//import com.lms.progress.service.KafkaProducerService;   // ✅ FIXED import
//import com.lms.progress.model.Progress;
//import com.lms.progress.repository.ProgressRepository;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//
//@Service
//public class ProgressService {
//
//    private final ProgressRepository repo;
//    private final KafkaProducerService producer;  // ✅ FIXED type
//
//    public ProgressService(ProgressRepository repo, KafkaProducerService producer) {
//        this.repo = repo;
//        this.producer = producer;
//    }
//
//    public ProgressResponse create(ProgressRequest req) {
//        Progress p = new Progress();
//        p.setUserId(req.getUserId());
//        p.setCourseId(req.getCourseId());
//        p.setCompletedContentIds(req.getCompletedContentIds());
//        p.setProgressPercentage(req.getProgressPercentage());
//        p.setUpdatedAt(Instant.now());
//
//        Progress saved = repo.save(p);
//
//        if (saved.getProgressPercentage() >= 100) {
//            producer.sendProgressEvent("User " + saved.getUserId() +
//                    " completed course " + saved.getCourseId());
//        }
//
//        return toResponse(saved);
//    }
//
//    public ProgressResponse getById(Long id) {
//        Progress p = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Progress not found: " + id));
//        return toResponse(p);
//    }
//
//    public ProgressResponse getByUserAndCourse(Long userId, Long courseId) {
//        Progress p = repo.findByUserIdAndCourseId(userId, courseId)
//                .orElseThrow(() -> new RuntimeException("Progress not found"));
//        return toResponse(p);
//    }
//
//    public ProgressResponse update(Long id, ProgressRequest req) {
//        Progress p = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Progress not found: " + id));
//
//        double before = p.getProgressPercentage();
//
//        if (req.getUserId() != null) p.setUserId(req.getUserId());
//        if (req.getCourseId() != null) p.setCourseId(req.getCourseId());
//        if (req.getCompletedContentIds() != null)
//            p.setCompletedContentIds(req.getCompletedContentIds());
//
//        p.setProgressPercentage(req.getProgressPercentage());
//        p.setUpdatedAt(Instant.now());
//
//        Progress saved = repo.save(p);
//
//        if (before < 100 && saved.getProgressPercentage() >= 100) {
//            producer.sendProgressEvent("User " + saved.getUserId() +
//                    " completed course " + saved.getCourseId());
//        }
//
//        return toResponse(saved);
//    }
//
//    public void delete(Long id) {
//        repo.deleteById(id);
//    }
//
//    private ProgressResponse toResponse(Progress p) {
//        ProgressResponse r = new ProgressResponse();
//        r.setProgressId(p.getId());
//        r.setUserId(p.getUserId());
//        r.setCourseId(p.getCourseId());
//        r.setCompletedContentIds(p.getCompletedContentIds());
//        r.setProgressPercentage(p.getProgressPercentage());
//        r.setUpdatedAt(p.getUpdatedAt());
//        return r;
//    }
//}
package com.lms.progress.service;

import com.lms.progress.dto.ProgressRequest;
import com.lms.progress.dto.ProgressResponse;
import com.lms.progress.model.Progress;
import com.lms.progress.repository.ProgressRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProgressService {

    private final ProgressRepository repo;
    private final KafkaProducerService producer;

    public ProgressService(ProgressRepository repo, KafkaProducerService producer) {
        this.repo = repo;
        this.producer = producer;
    }

    public ProgressResponse create(ProgressRequest req) {
        Progress p = new Progress();

        p.setStudentEmail(req.getStudentEmail());
        p.setCourseId(req.getCourseId());
        p.setCompletedContentIds(req.getCompletedContentIds());
        p.setProgressPercentage(req.getProgressPercentage());
        p.setUpdatedAt(Instant.now());

        Progress saved = repo.save(p);

        if (saved.getProgressPercentage() >= 100) {
            producer.sendProgressEvent(
                "User " + saved.getStudentEmail() +
                " completed course " + saved.getCourseId()
            );
        }

        return toResponse(saved);
    }

    public ProgressResponse getById(Long id) {
        Progress p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progress not found: " + id));
        return toResponse(p);
    }

    public ProgressResponse getByUserAndCourse(String email, Long courseId) {
        Progress p = repo.findByStudentEmailAndCourseId(email, courseId)
                .orElseThrow(() -> new RuntimeException("Progress not found"));

        return toResponse(p);
    }

    public ProgressResponse update(Long id, ProgressRequest req) {
        Progress p = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Progress not found"));

        double before = p.getProgressPercentage();

        if (req.getStudentEmail() != null)
            p.setStudentEmail(req.getStudentEmail());

        if (req.getCourseId() != null)
            p.setCourseId(req.getCourseId());

        if (req.getCompletedContentIds() != null)
            p.setCompletedContentIds(req.getCompletedContentIds());

        p.setProgressPercentage(req.getProgressPercentage());
        p.setUpdatedAt(Instant.now());

        Progress saved = repo.save(p);

        if (before < 100 && saved.getProgressPercentage() >= 100) {
            producer.sendProgressEvent(
                "User " + saved.getStudentEmail() +
                " completed course " + saved.getCourseId()
            );
        }

        return toResponse(saved);
    }

    public void deleteByEmail(String email) {
        repo.deleteByStudentEmail(email);

        producer.sendProgressEvent(
            "Progress deleted for user: " + email
        );
    }

    public void deleteById(Long id) {
        repo.deleteById(id);

        producer.sendProgressEvent(
            "Progress deleted for ID: " + id
        );
    }

    private ProgressResponse toResponse(Progress p) {
        ProgressResponse r = new ProgressResponse();

        r.setProgressId(p.getId());
        r.setStudentEmail(p.getStudentEmail());
        r.setCourseId(p.getCourseId());
        r.setCompletedContentIds(p.getCompletedContentIds());
        r.setProgressPercentage(p.getProgressPercentage());
        r.setUpdatedAt(p.getUpdatedAt());

        return r;
    }
}