//package com.lms.course.service;
//
//import com.lms.course.dto.ContentEvent;
//import com.lms.course.kafka.ContentEventProducer;
//import com.lms.course.model.ContentItem;
//import com.lms.course.repository.ContentRepository;
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
//
//    public ContentService(ContentRepository repo, ContentEventProducer producer) {
//        this.repo = repo;
//        this.producer = producer;
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
//            producer.sendEvent(new ContentEvent(
//                    "CONTENT_CREATED",
//                    Map.of(
//                            "id", saved.getId(),
//                            "courseId", saved.getCourseId(),
//                            "title", saved.getTitle(),
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
//    public List<ContentItem> getPublicByCourse(Long courseId) {
//        return repo.findByCourseId(courseId);
//    }
//
//    public List<ContentItem> getByCourseForStudents(Long courseId) {
//        return repo.findByCourseId(courseId);
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

    private final ContentRepository repo;
    private final ContentEventProducer producer;
    private final CourseRepository courseRepo;        // ← added

    public ContentService(ContentRepository repo,
                          ContentEventProducer producer,
                          CourseRepository courseRepo) {  // ← added
        this.repo = repo;
        this.producer = producer;
        this.courseRepo = courseRepo;                 // ← added
    }

    // ============================
    // CREATE CONTENT  (SAFE)
    // ============================
    @CacheEvict(value = "contentByCourse", allEntries = true)
    public ContentItem create(ContentItem item, String email) {

        // 🔒 Validation (prevents NPE / SpEL crash)
        if (item.getCourseId() == null) {
            throw new RuntimeException("courseId is required");
        }

        if (item.getTitle() == null || item.getTitle().isBlank()) {
            throw new RuntimeException("title is required");
        }

        item.setOwnerEmail(email);

        ContentItem saved = repo.save(item);

        // 🔥 Kafka must NEVER break REST
        try {
            Course course = courseRepo.findById(saved.getCourseId())  // ← added
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            producer.sendEvent(new ContentEvent(
                    "CONTENT_CREATED",
                    Map.of(
                            "id",         saved.getId(),
                            "courseId",   saved.getCourseId(),
                            "batchId",    course.getBatchId(),   // ← added
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
    // GET CONTENT BY COURSE
    // ============================
    @Cacheable(value = "contentByCourse", key = "#courseId")
    public List<ContentItem> getByCourse(Long courseId, String email) {

        if (courseId == null) {
            throw new RuntimeException("courseId is required");
        }

        // 🔥 Trainer / Student / Preview must all see same content
        return repo.findByCourseId(courseId);
    }


    // ============================
    // UPDATE CONTENT
    // ============================
    @CacheEvict(value = "contentByCourse", allEntries = true)
    public ContentItem update(Long id, ContentItem updated, String email) {

        ContentItem existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!existing.getOwnerEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        if (updated.getTitle() != null)
            existing.setTitle(updated.getTitle());

        if (updated.getDescription() != null)
            existing.setDescription(updated.getDescription());

        if (updated.getContentType() != null)
            existing.setContentType(updated.getContentType());

        if (updated.getUrl() != null)
            existing.setUrl(updated.getUrl());

        if (updated.getDurationSeconds() != null)
            existing.setDurationSeconds(updated.getDurationSeconds());

        if (updated.getOrderIndex() != null)
            existing.setOrderIndex(updated.getOrderIndex());

        return repo.save(existing);
    }

    // ============================
    // DELETE CONTENT
    // ============================
    @CacheEvict(value = "contentByCourse", allEntries = true)
    public String delete(Long id, String email) {

        ContentItem existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        if (!existing.getOwnerEmail().equals(email)) {
            throw new RuntimeException("Unauthorized");
        }

        repo.delete(existing);
        return "Content deleted";
    }

    public List<ContentItem> getPublicByCourse(Long courseId) {
        return repo.findByCourseId(courseId);
    }

    public List<ContentItem> getByCourseForStudents(Long courseId) {
        return repo.findByCourseId(courseId);
    }

}