//
//
//
//package com.lms.course.service;
//import com.lms.course.dto.ContentEvent;
//import java.util.Map;
//import com.lms.course.dto.ContentEvent;
//import com.lms.course.kafka.ContentEventProducer;
//import com.lms.course.model.ContentItem;
//import com.lms.course.model.Course;
//import com.lms.course.repository.ContentRepository;
//import com.lms.course.repository.CourseRepository;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class ContentService {
//
//    private final ContentRepository repo;
//    private final ContentEventProducer producer;
//    private final CourseRepository courseRepo;        // ← added
//
//    public ContentService(ContentRepository repo,
//                          ContentEventProducer producer,
//                          CourseRepository courseRepo) {  // ← added
//        this.repo = repo;
//        this.producer = producer;
//        this.courseRepo = courseRepo;                 // ← added
//    }
//
//    // ============================
//    // CREATE CONTENT  (SAFE)
//    // ============================
//    @CacheEvict(value = "contentByCourse", allEntries = true)
//    public ContentItem create(ContentItem item, String email) {
//
//        // 🔒 Validation (prevents NPE / SpEL crash)
//        if (item.getCourseId() == null) {
//            throw new RuntimeException("courseId is required");
//        }
//
//        if (item.getTitle() == null || item.getTitle().isBlank()) {
//            throw new RuntimeException("title is required");
//        }
//
//        item.setOwnerEmail(email);
//
//        ContentItem saved = repo.save(item);
//
//        // 🔥 Kafka must NEVER break REST
//        try {
//            Course course = courseRepo.findById(saved.getCourseId())  // ← added
//                    .orElseThrow(() -> new RuntimeException("Course not found"));
//
//            producer.sendEvent(new ContentEvent(
//                    "CONTENT_CREATED",
//                    Map.of(
//                            "id",         saved.getId(),
//                            "courseId",   saved.getCourseId(),
//                            "batchId",    course.getBatchId(),   // ← added
//                            "title",      saved.getTitle(),
//                            "ownerEmail", saved.getOwnerEmail()
//                    )
//            ));
//        } catch (Exception e) {
//            System.out.println("Kafka unavailable, skipping CONTENT_CREATED event");
//        }
//
//        return saved;
//    }
//
//    // ============================
//    // GET CONTENT BY COURSE
//    // ============================
//    @Cacheable(value = "contentByCourse", key = "#courseId")
//    public List<ContentItem> getByCourse(Long courseId, String email) {
//
//        if (courseId == null) {
//            throw new RuntimeException("courseId is required");
//        }
//
//        // 🔥 Trainer / Student / Preview must all see same content
//        return repo.findByCourseId(courseId);
//    }
//
//
//    // ============================
//    // UPDATE CONTENT
//    // ============================
//    @CacheEvict(value = "contentByCourse", allEntries = true)
//    public ContentItem update(Long id, ContentItem updated, String email) {
//
//        ContentItem existing = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Content not found"));
//
//        if (!existing.getOwnerEmail().equals(email)) {
//            throw new RuntimeException("Unauthorized");
//        }
//
//        if (updated.getTitle() != null)
//            existing.setTitle(updated.getTitle());
//
//        if (updated.getDescription() != null)
//            existing.setDescription(updated.getDescription());
//
//        if (updated.getContentType() != null)
//            existing.setContentType(updated.getContentType());
//
//        if (updated.getUrl() != null)
//            existing.setUrl(updated.getUrl());
//
//        if (updated.getDurationSeconds() != null)
//            existing.setDurationSeconds(updated.getDurationSeconds());
//
//        if (updated.getOrderIndex() != null)
//            existing.setOrderIndex(updated.getOrderIndex());
//
//        return repo.save(existing);
//    }
//
//    // ============================
//    // DELETE CONTENT
//    // ============================
//    @CacheEvict(value = "contentByCourse", allEntries = true)
//    public String delete(Long id, String email) {
//
//        ContentItem existing = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Content not found"));
//
//        if (!existing.getOwnerEmail().equals(email)) {
//            throw new RuntimeException("Unauthorized");
//        }
//
//        repo.delete(existing);
//        return "Content deleted";
//    }
//
//    public List<ContentItem> getPublicByCourse(Long courseId) {
//        return repo.findByCourseId(courseId);
//    }
//
//    public List<ContentItem> getByCourseForStudents(Long courseId) {
//        return repo.findByCourseId(courseId);
//    }
// // Add new method at bottom of ContentService
//    public void markContentComplete(Long contentId, String studentEmail) {
//
//        // 1. Find the content item
//        ContentItem item = repo.findById(contentId)
//                .orElseThrow(() -> new RuntimeException("Content not found"));
//
//        // 2. Get total content count for this course (you already have countByCourseId!)
//        long totalContent = repo.countByCourseId(item.getCourseId());
//
//        // 3. Fire Kafka event to progress-service
//        try {
//            producer.sendEvent(new ContentEvent(
//                    "CONTENT_COMPLETED",
//                    Map.of(
//                            "contentId",    item.getId(),
//                            "courseId",     item.getCourseId(),
//                            "studentEmail", studentEmail,
//                            "totalContent", totalContent
//                    )
//            ));
//        } catch (Exception e) {
//            System.out.println("Kafka unavailable, skipping CONTENT_COMPLETED event");
//        }
//    }
//
//}




package com.lms.course.service;

import com.lms.course.dto.ContentEvent;
import com.lms.course.kafka.ContentEventProducer;
import com.lms.course.model.ContentItem;
import com.lms.course.model.Course;
import com.lms.course.repository.ContentRepository;
import com.lms.course.repository.CourseRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ContentService {

    private final ContentRepository    repo;
    private final ContentEventProducer producer;
    private final CourseRepository     courseRepo;

    public ContentService(ContentRepository repo,
                          ContentEventProducer producer,
                          CourseRepository courseRepo) {
        this.repo       = repo;
        this.producer   = producer;
        this.courseRepo = courseRepo;
    }

    // ============================
    // CREATE
    // ============================
    @CacheEvict(value = "contentByCourse", allEntries = true)
    public ContentItem create(ContentItem item, String email) {

        if (item.getCourseId() == null)
            throw new RuntimeException("courseId is required");

        if (item.getTitle() == null || item.getTitle().isBlank())
            throw new RuntimeException("title is required");

        item.setOwnerEmail(email);
        ContentItem saved = repo.save(item);

        try {
            Course course = courseRepo.findById(saved.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            producer.sendEvent(new ContentEvent(
                "CONTENT_CREATED",
                Map.of(
                    "id",         saved.getId(),
                    "courseId",   saved.getCourseId(),
                    "batchId",    course.getBatchId(),
                    "title",      saved.getTitle(),
                    "ownerEmail", saved.getOwnerEmail()
                )
            ));
        } catch (Exception e) {
            System.out.println("Kafka unavailable, skipping CONTENT_CREATED event");
        }

        return saved;
    }

    // ============================
    // GET BY COURSE
    // ============================
    @Cacheable(value = "contentByCourse", key = "#courseId")
    public List<ContentItem> getByCourse(Long courseId, String email) {
        if (courseId == null)
            throw new RuntimeException("courseId is required");
        return repo.findByCourseId(courseId);
    }

    // ============================
    // UPDATE  ← FIXED
    // ============================
    // What changed:
    //   - Captures the OLD url before overwriting it.
    //   - After saving, if the url actually changed, fires CONTENT_UPDATED
    //     so video-service / file-service can delete the old physical file.
    @CacheEvict(value = "contentByCourse", allEntries = true)
    public ContentItem update(Long id, ContentItem updated, String email) {

        ContentItem existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!existing.getOwnerEmail().equals(email))
            throw new RuntimeException("Unauthorized");

        // ── snapshot old URL before any changes ──────────────────────────────
        String oldUrl         = existing.getUrl();
        String oldContentType = existing.getContentType();

        if (updated.getTitle()           != null) existing.setTitle(updated.getTitle());
        if (updated.getDescription()     != null) existing.setDescription(updated.getDescription());
        if (updated.getContentType()     != null) existing.setContentType(updated.getContentType());
        if (updated.getUrl()             != null) existing.setUrl(updated.getUrl());
        if (updated.getDurationSeconds() != null) existing.setDurationSeconds(updated.getDurationSeconds());
        if (updated.getOrderIndex()      != null) existing.setOrderIndex(updated.getOrderIndex());

        ContentItem saved = repo.save(existing);

        // ── fire CONTENT_UPDATED only when the URL actually changed ───────────
        // "URL changed" means a new file was uploaded by the frontend before
        // calling this endpoint. The old physical file still exists in
        // video-service / file-service and must be cleaned up.
        boolean urlChanged = updated.getUrl() != null
                          && !updated.getUrl().equals(oldUrl);

        if (urlChanged) {
            try {
                producer.publishContentUpdated(
                    saved.getId(),
                    saved.getCourseId(),
                    oldUrl,                              // video/file service deletes this
                    saved.getUrl(),                      // new URL (already uploaded)
                    oldContentType != null
                        ? oldContentType
                        : saved.getContentType()
                );
            } catch (Exception e) {
                System.out.println("Kafka unavailable, skipping CONTENT_UPDATED event");
            }
        }

        return saved;
    }

    // ============================
    // DELETE  ← FIXED
    // ============================
    // What changed:
    //   - After removing the ContentItem, fires CONTENT_DELETED so
    //     video-service / file-service delete the physical file + their DB row.
    @CacheEvict(value = "contentByCourse", allEntries = true)
    public String delete(Long id, String email) {

        ContentItem existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!existing.getOwnerEmail().equals(email))
            throw new RuntimeException("Unauthorized");

        // ── snapshot before delete ────────────────────────────────────────────
        Long   courseId    = existing.getCourseId();
        String url         = existing.getUrl();
        String contentType = existing.getContentType();

        repo.delete(existing);

        // ── tell video-service or file-service to clean up ───────────────────
        try {
            producer.publishContentDeleted(id, courseId, url, contentType);
        } catch (Exception e) {
            System.out.println("Kafka unavailable, skipping CONTENT_DELETED event");
        }

        return "Content deleted";
    }

    // ============================
    // MARK COMPLETE
    // ============================
    public void markContentComplete(Long contentId, String studentEmail) {

        ContentItem item = repo.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        long totalContent = repo.countByCourseId(item.getCourseId());

        try {
            producer.sendEvent(new ContentEvent(
                "CONTENT_COMPLETED",
                Map.of(
                    "contentId",    item.getId(),
                    "courseId",     item.getCourseId(),
                    "studentEmail", studentEmail,
                    "totalContent", totalContent
                )
            ));
        } catch (Exception e) {
            System.out.println("Kafka unavailable, skipping CONTENT_COMPLETED event");
        }
    }

    // ============================
    // PUBLIC / STUDENT VIEWS
    // ============================
    public List<ContentItem> getPublicByCourse(Long courseId) {
        return repo.findByCourseId(courseId);
    }

    public List<ContentItem> getByCourseForStudents(Long courseId) {
        return repo.findByCourseId(courseId);
    }
}