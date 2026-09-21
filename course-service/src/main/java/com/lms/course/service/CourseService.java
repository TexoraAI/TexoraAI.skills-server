package com.lms.course.service;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.lms.course.constants.CourseTierLimits;
import com.lms.course.constants.CourseTierResolver;
import com.lms.course.dto.CourseEvent;
import com.lms.course.exception.CourseCountLimitExceededException;
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
    private final CourseTierResolver courseTierResolver; // NEW

    public CourseService(
            CourseRepository repo,
            CourseEventProducer producer,
            TrainerBatchMapRepository trainerBatchRepo,
            StudentBatchMapRepository studentBatchRepo,
            CourseTierResolver courseTierResolver) { // NEW param

        this.repo             = repo;
        this.producer         = producer;
        this.trainerBatchRepo = trainerBatchRepo;
        this.studentBatchRepo = studentBatchRepo;
        this.courseTierResolver = courseTierResolver; // NEW
    }

    // ============================
    // CREATE COURSE
    // ============================
    // NEW — accepts organizationId extracted from JWT in CourseController.
    // For org-based trainers  : validates trainerEmail + batchId + organizationId (tenant isolation).
    // For non-org trainers     : falls back to existing trainerEmail + batchId check (no change in behavior).
    @CacheEvict(value = "coursesByEmail", key = "#email")
    public Course create(Course course, String email, String organizationId) {

        // NEW — plan-based course count check, FIRST check before batch-assignment validation
        String tier = courseTierResolver.resolveTier(organizationId, email);
        long currentCount = repo.countByOwnerEmail(email);
        int maxCourses = CourseTierLimits.maxCoursesFor(tier);
        if (currentCount >= maxCourses) {
            throw new CourseCountLimitExceededException((int) currentCount, maxCourses, tier);
        }

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

//    // ============================
//    // GET STUDENT COURSES
//    // ============================

    // ============================
    // GET STUDENT COURSES
    // ============================
    // FIXED — now accepts organizationId (passed from the controller's JWT
    // extraction, same pattern as create()) so org-enrolled students resolve
    // their tier via their ORG's plan, not a nonexistent personal plan.
    // Independent students still resolve correctly since resolveTier()
    // falls back to the email/UserPlanCache lookup when organizationId is null.
    public List<Course> getStudentCourses(String studentEmail, String organizationId) {

        List<Long> batchIds = studentBatchRepo
                .findByStudentEmail(studentEmail)
                .stream()
                .map(map -> map.getBatchId())
                .toList();

        if (batchIds.isEmpty()) {
            return List.of();
        }

        List<Course> all = repo.findByBatchIdIn(batchIds);

        // Plan-tier cap on how many enrolled courses a student can see.
        // Truncates rather than throwing, same reasoning as
        // video-service/file-service: viewing a list isn't a gated
        // "action", so a silent cap plus a companion count endpoint
        // (getStudentCourseCount) is the correct UX here.
        String tier = courseTierResolver.resolveTier(organizationId, studentEmail);
        int visibleCap = CourseTierLimits.studentVisibleCountFor(tier);
        if (visibleCap == -1) {
            return all;
        }
        return all.stream().limit(visibleCap).toList();
    }

    // Companion to getStudentCourses(): reports the TRUE total (before the
    // tier cap) so the frontend can render an "Upgrade to unlock N more"
    // tile instead of silently stopping. Same organizationId fix as above.
    public java.util.Map<String, Object> getStudentCourseCount(String studentEmail, String organizationId) {

        List<Long> batchIds = studentBatchRepo
                .findByStudentEmail(studentEmail)
                .stream()
                .map(map -> map.getBatchId())
                .toList();

        int totalCount = batchIds.isEmpty() ? 0 : repo.findByBatchIdIn(batchIds).size();

        String tier = courseTierResolver.resolveTier(organizationId, studentEmail);
        int visibleCap = CourseTierLimits.studentVisibleCountFor(tier);
        int visibleCount = visibleCap == -1 ? totalCount : Math.min(totalCount, visibleCap);

        return java.util.Map.of(
                "totalCount", totalCount,
                "visibleCount", visibleCount,
                "tier", tier,
                "unlimited", visibleCap == -1
        );
    }

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

 // NEW — plan-based course count check, scoped to the trainer being assigned,
 // tier resolved via org (adminCreate always has an organizationId in this flow)
 String tier = courseTierResolver.resolveTier(organizationId, course.getAssignedTrainerEmail());
 long currentCount = repo.countByOrganizationIdAndAssignedTrainerEmail(
         organizationId, course.getAssignedTrainerEmail());
 int maxCourses = CourseTierLimits.maxCoursesFor(tier);
 if (currentCount >= maxCourses) {
     throw new CourseCountLimitExceededException((int) currentCount, maxCourses, tier);
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

public List<Course> getTrainerAllCourses(String trainerEmail, String organizationId) {

    // Independent trainer (no organization)
    if (organizationId == null || organizationId.isBlank()) {
        return repo.findByOwnerEmail(trainerEmail);
    }

    // Organization trainer - own courses
    List<Course> own = repo.findByOwnerEmail(trainerEmail)
            .stream()
            .filter(c -> organizationId.equals(c.getOrganizationId()))
            .collect(Collectors.toList());
    // Organization trainer - assigned courses
    List<Course> assigned = repo.findByAssignedTrainerEmailAndOrganizationId(
            trainerEmail,
            organizationId
    );
    // Merge without duplicates
    Map<Long, Course> merged = new LinkedHashMap<>();
    own.forEach(c -> merged.put(c.getId(), c));
    assigned.forEach(c -> merged.putIfAbsent(c.getId(), c));
    return new ArrayList<>(merged.values());
}

//============================
//GET TRAINER COURSE USAGE (for quota pill on Create Course page)
//============================
public java.util.Map<String, Object> getCourseUsage(String email, String organizationId) {

 String tier = courseTierResolver.resolveTier(organizationId, email);
 long used = repo.countByOwnerEmail(email);
 int limit = CourseTierLimits.maxCoursesFor(tier);

 return java.util.Map.of(
         "tier", tier,
         "used", used,
         "limit", limit,
         "remaining", Math.max(0, limit - used),
         "period", "lifetime" // matches your course-limit semantics — no reset window currently
 );
}
//============================
//STUDENT: plan summary — course visibility cap + module cap per course
//============================
public java.util.Map<String, Object> getStudentPlanSummary(String studentEmail, String organizationId) {
 String tier = courseTierResolver.resolveTier(organizationId, studentEmail);
 int courseVisibleCap = CourseTierLimits.studentVisibleCountFor(tier);
 int moduleCapPerCourse = CourseTierLimits.maxModulesPerCourseFor(tier);

 return java.util.Map.of(
         "tier", tier,
         "coursesVisible", courseVisibleCap == -1 ? "unlimited" : courseVisibleCap,
         "modulesPerCourse", moduleCapPerCourse
 );
}

             
}