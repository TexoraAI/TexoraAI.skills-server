

package com.lms.course.service;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.lms.course.dto.CourseEvent;
import com.lms.course.kafka.CourseEventProducer;
import com.lms.course.model.Course;
import com.lms.course.repository.CourseRepository;
import com.lms.course.repository.TrainerBatchMapRepository;
import com.lms.course.repository.StudentBatchMapRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;
@Service
public class CourseService {

    private final CourseRepository repo;
    private final CourseEventProducer producer;
    private final TrainerBatchMapRepository trainerBatchRepo;
    private final StudentBatchMapRepository studentBatchRepo;

    public CourseService(
            CourseRepository repo,
            CourseEventProducer producer,
            TrainerBatchMapRepository trainerBatchRepo,
            StudentBatchMapRepository studentBatchRepo) {

        this.repo             = repo;
        this.producer         = producer;
        this.trainerBatchRepo = trainerBatchRepo;
        this.studentBatchRepo = studentBatchRepo;
    }

    // ============================
    // CREATE COURSE
    // ============================
    // NEW — accepts organizationId extracted from JWT in CourseController.
    // For org-based trainers  : validates trainerEmail + batchId + organizationId (tenant isolation).
    // For non-org trainers     : falls back to existing trainerEmail + batchId check (no change in behavior).
    @CacheEvict(value = "coursesByEmail", key = "#email")
    public Course create(Course course, String email, String organizationId) {

        course.setOwnerEmail(email);

        if (organizationId != null) {
            // Org-based trainer — enforce tenant isolation
            boolean assigned = trainerBatchRepo
                    .existsByTrainerEmailAndBatchIdAndOrganizationId(
                            email, course.getBatchId(), organizationId);

            if (!assigned) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Trainer not assigned to this batch in your organization");
            }
        } else {
            // Non-org trainer (super admin / Google / self-registered) — existing behavior
            boolean assigned = trainerBatchRepo
                    .existsByTrainerEmailAndBatchId(email, course.getBatchId());

            if (!assigned) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Trainer not assigned to this batch");
            }
        }

        // NEW — store organizationId on the course (null for non-org users)
        course.setOrganizationId(organizationId);

        Course saved = repo.save(course);

        try {
            producer.send(new CourseEvent(
                    "COURSE_CREATED",
                    Map.of(
                            "courseId",        saved.getId(),
                            "title",           saved.getTitle(),
                            "ownerEmail",      saved.getOwnerEmail(),
                            "batchId",         saved.getBatchId(),
                            "organizationId",  saved.getOrganizationId() != null
                                                   ? saved.getOrganizationId()
                                                   : ""   // downstream consumers handle empty string as no-org
                    )
            ));
        } catch (Exception e) {
            System.out.println("Kafka unavailable, skipping COURSE_CREATED event");
        }

        return saved;
    }

    // ============================
    // GET BY EMAIL (Trainer Only)
    // ============================
    @Cacheable(value = "coursesByEmail", key = "#email")
    public List<Course> getByEmail(String email) {
        return repo.findByOwnerEmail(email);
    }

    // ============================
    // GET BY ID (Student Validation)
    // ============================
    @Cacheable(value = "courseById", key = "#id")
    public Course getById(Long id, String email, String role) {

        Course course = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));

        if ("STUDENT".equalsIgnoreCase(role)) {
            boolean allowed = studentBatchRepo
                    .existsByStudentEmailAndBatchId(email, course.getBatchId());
            if (!allowed) {
                throw new RuntimeException("Student not assigned to this batch");
            }
        }

        return course;
    }

    // ============================
    // UPDATE COURSE
    // ============================
    @CacheEvict(value = {"courseById", "coursesByEmail", "allCourses"}, allEntries = true)
    public Course update(Long id, Course updated) {

        Course existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (updated.getTitle() != null)
            existing.setTitle(updated.getTitle());

        if (updated.getDescription() != null)
            existing.setDescription(updated.getDescription());

        if (updated.getCategory() != null)
            existing.setCategory(updated.getCategory());

        Course saved = repo.save(existing);

        producer.publishCourseUpdated(saved.getId());

        return saved;
    }

    // ============================
    // DELETE COURSE
    // ============================
    @CacheEvict(value = {"courseById", "coursesByEmail", "allCourses"}, allEntries = true)
    public String delete(Long id) {

        if (!repo.existsById(id)) {
            return "Course not found";
        }

        repo.deleteById(id);

        producer.publishCourseDeleted(id);

        return "Course deleted successfully";
    }

    // ============================
    // GET TRAINER COURSES
    // ============================
    public List<Course> getTrainerCourses(String email) {

        List<Long> batchIds = trainerBatchRepo
                .findByTrainerEmail(email)
                .stream()
                .map(m -> m.getBatchId())
                .toList();

        return repo.findByBatchIdIn(batchIds);
    }

    // ============================
    // GET STUDENT COURSES
    // ============================
    public List<Course> getStudentCourses(String studentEmail) {

        List<Long> batchIds = studentBatchRepo
                .findByStudentEmail(studentEmail)
                .stream()
                .map(map -> map.getBatchId())
                .toList();

        if (batchIds.isEmpty()) {
            return List.of();
        }

        return repo.findByBatchIdIn(batchIds);
    }

//    // ============================
//    // ADMIN - LIST ALL COURSES
//    // ============================
//    @Cacheable(value = "allCourses")
//    public List<Course> getAllCoursesForAdmin() {
//        return repo.findAllByOrderByCreatedAtDesc();
//    }
//
//    // ============================
//    // ADMIN - GET COURSES BY CATEGORY
//    // ============================
//    @Cacheable(value = "coursesByCategory", key = "#category")
//    public List<Course> getByCategory(String category) {
//
//        if (category == null || category.isBlank()) {
//            throw new RuntimeException("Category is required");
//        }
//
//        return repo.findByCategoryIgnoreCase(category);
//    }
    
 // GET COURSES BY ORGANIZATION
 // ============================
 public List<Course> getCoursesByOrganization(String organizationId) {
     if (organizationId == null || organizationId.isBlank())
         throw new RuntimeException("organizationId is required");
     return repo.findByOrganizationId(organizationId);
 }
 
//GET ALL CATEGORIES (super admin)
//============================
public List<String> getAllCategories() {
  return repo.findAllDistinctCategories();
}

//============================
//SUPER ADMIN — independent trainer courses only (organizationId IS NULL)
//============================
public List<Course> getIndependentTrainerCourses() {
return repo.findByOrganizationIdIsNull();
}
//SUPER ADMIN — categories from independent trainer courses only
public List<String> getIndependentTrainerCategories() {
 return repo.findDistinctCategoryByOrganizationIdIsNull();
}

//Admin creates + assigns course to a trainer
//============================
//ADMIN: Create + assign course to a trainer (no batch validation)
//============================
public Course adminCreate(Course course, String adminEmail, String organizationId) {
 if (course.getAssignedTrainerEmail() == null
         || course.getAssignedTrainerEmail().isBlank()) {
     throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
             "assignedTrainerEmail is required");
 }
 course.setOwnerEmail(adminEmail);
 course.setOrganizationId(organizationId);
 return repo.save(course);
}

//============================
//ADMIN: click trainer email → see all courses assigned to that trainer
//============================
public List<Course> getCoursesByAssignedTrainer(
     String trainerEmail, String organizationId) {
 return repo.findByOrganizationIdAndAssignedTrainerEmail(
         organizationId, trainerEmail);
}

//============================
//TRAINER: own courses + admin-assigned courses merged
//============================
public List<Course> getTrainerAllCourses(
     String trainerEmail, String organizationId) {

 List<Course> own = repo.findByOwnerEmail(trainerEmail)
         .stream()
         .filter(c -> organizationId.equals(c.getOrganizationId()))
         .collect(Collectors.toList());

 List<Course> assigned = repo.findByAssignedTrainerEmailAndOrganizationId(
         trainerEmail, organizationId);

 // merge + deduplicate by id
 Map<Long, Course> merged = new LinkedHashMap<>();
 own.forEach(c -> merged.put(c.getId(), c));
 assigned.forEach(c -> merged.putIfAbsent(c.getId(), c));

 return new ArrayList<>(merged.values());
}
}