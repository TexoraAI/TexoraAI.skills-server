




package com.lms.course.service;

import com.lms.course.dto.CourseEvent;
import com.lms.course.kafka.CourseEventProducer;
import com.lms.course.model.Course;
import com.lms.course.repository.CourseRepository;
import com.lms.course.repository.TrainerBatchMapRepository;
import com.lms.course.repository.StudentBatchMapRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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

        this.repo = repo;
        this.producer = producer;
        this.trainerBatchRepo = trainerBatchRepo;
        this.studentBatchRepo = studentBatchRepo;
    }

    // ============================
    // CREATE COURSE (Trainer Validation)
    // ============================
    @CacheEvict(value = "coursesByEmail", key = "#email")
    public Course create(Course course, String email) {

        course.setOwnerEmail(email);

        boolean assigned =
                trainerBatchRepo.existsByTrainerEmailAndBatchId(
                        email, course.getBatchId());

        if (!assigned) {
            throw new RuntimeException("Trainer not assigned to this batch");
        }

        Course saved = repo.save(course);

        try {
            producer.send(new CourseEvent(
                    "COURSE_CREATED",
                    Map.of(
                            "courseId", saved.getId(),
                            "title", saved.getTitle(),
                            "ownerEmail", saved.getOwnerEmail(),
                            "batchId", saved.getBatchId()
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
//    // LIST ALL (Optional - Keep Global)
//    // ============================
//    @Cacheable(value = "allCourses")
//    public List<Course> listAll() {
//        return repo.findAll();
//    }

    // ============================
    // GET BY ID (Student Validation Added)
    // ============================
    @Cacheable(value = "courseById", key = "#id")
    public Course getById(Long id, String email, String role) {

        Course course = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id " + id));

        // 🔐 If STUDENT → validate batch access
        if ("STUDENT".equalsIgnoreCase(role)) {

            boolean allowed =
                    studentBatchRepo.existsByStudentEmailAndBatchId(
                            email, course.getBatchId());

            if (!allowed) {
                throw new RuntimeException("Student not assigned to this batch");
            }
        }

        return course;
    }

    // ============================
    // UPDATE COURSE
//    // ============================
//    @CacheEvict(value = {"courseById", "coursesByEmail", "allCourses"}, allEntries = true)
//    public Course update(Long id, Course updated) {
//
//        Course existing = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Course not found"));
//
//        if (updated.getTitle() != null)
//            existing.setTitle(updated.getTitle());
//
//        if (updated.getDescription() != null)
//            existing.setDescription(updated.getDescription());
//
//        if (updated.getCategory() != null)
//            existing.setCategory(updated.getCategory());
//
//        return repo.save(existing);
//    }

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

        // 🔥 Publish Kafka event
        producer.publishCourseUpdated(saved.getId());

        return saved;
    }
    
    
    
    // ============================
    // DELETE COURSE
//    // ============================
//    @CacheEvict(value = {"courseById", "coursesByEmail", "allCourses"}, allEntries = true)
//    public String delete(Long id) {
//        repo.deleteById(id);
//        return "Course deleted successfully";
//    }
    @CacheEvict(value = {"courseById", "coursesByEmail", "allCourses"}, allEntries = true)
    public String delete(Long id) {

        if (!repo.existsById(id)) {
            return "Course not found";
        }

        // 1️⃣ Delete from DB
        repo.deleteById(id);

        // 2️⃣ Publish Kafka event for cleanup
        producer.publishCourseDeleted(id);

        return "Course deleted successfully";
    }
    public List<Course> getTrainerCourses(String email) {

        List<Long> batchIds = trainerBatchRepo
                .findByTrainerEmail(email)
                .stream()
                .map(m -> m.getBatchId())
                .toList();

        return repo.findByBatchIdIn(batchIds);
    }
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
    
 // ============================
 // ADMIN - LIST ALL COURSES
 // ============================
 @Cacheable(value = "allCourses")
	 public List<Course> getAllCoursesForAdmin() {
		    return repo.findAllByOrderByCreatedAtDesc();
 }
 
//============================
//ADMIN - GET COURSES BY CATEGORY
//============================
@Cacheable(value = "coursesByCategory", key = "#category")
public List<Course> getByCategory(String category) {

  if (category == null || category.isBlank()) {
      throw new RuntimeException("Category is required");
  }

  return repo.findByCategoryIgnoreCase(category);
}
}