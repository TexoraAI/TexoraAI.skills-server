package com.lms.video.repository;

import com.lms.video.model.CourseVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseVideoRepository extends JpaRepository<CourseVideo, Long> {

    List<CourseVideo> findByCourseId(Long courseId);

    List<CourseVideo> findByModuleId(Long moduleId);

    void deleteByModuleId(Long moduleId);

    void deleteByCourseId(Long courseId);
}