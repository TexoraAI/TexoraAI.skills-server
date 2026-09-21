package com.lms.video.repository;
import java.util.Optional;
import com.lms.video.model.CourseVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseVideoRepository extends JpaRepository<CourseVideo, Long> {

    List<CourseVideo> findByCourseId(Long courseId);

    List<CourseVideo> findByModuleId(Long moduleId);

    void deleteByModuleId(Long moduleId);

    void deleteByCourseId(Long courseId);
    Optional<CourseVideo> findByUrl(String url);

    // NEW — per-trainer storage usage for course-video plan enforcement.
    // Per-trainer scoping (not org-pooled) since CourseVideo has no organizationId column.
    @Query("SELECT COALESCE(SUM(cv.size), 0) FROM CourseVideo cv WHERE cv.uploadedBy = :email")
    long sumStorageUsageByTrainer(@Param("email") String email);
}