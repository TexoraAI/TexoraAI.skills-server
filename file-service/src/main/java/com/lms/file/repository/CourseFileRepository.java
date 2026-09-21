package com.lms.file.repository;
import com.lms.file.model.CourseFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface CourseFileRepository extends JpaRepository<CourseFile, Long> {
    List<CourseFile> findByCourseId(Long courseId);
    Optional<CourseFile> findByUrl(String url);

    // NEW — per-trainer storage usage for course-file plan enforcement.
    // Per-trainer scoping (not org-pooled) — CourseFile has no organizationId column.
    @Query("SELECT COALESCE(SUM(cf.size), 0) FROM CourseFile cf WHERE cf.uploadedBy = :email")
    long sumStorageUsageByTrainer(@Param("email") String email);
}