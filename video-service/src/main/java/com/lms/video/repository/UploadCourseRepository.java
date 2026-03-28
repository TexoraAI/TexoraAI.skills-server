package com.lms.video.repository;

import com.lms.video.model.UploadCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadCourseRepository extends JpaRepository<UploadCourse, Long> {

    List<UploadCourse> findByCourseId(Long courseId);
}