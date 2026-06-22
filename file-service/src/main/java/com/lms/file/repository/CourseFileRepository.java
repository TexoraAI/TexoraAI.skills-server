package com.lms.file.repository;

import com.lms.file.model.CourseFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface CourseFileRepository extends JpaRepository<CourseFile, Long> {

    List<CourseFile> findByCourseId(Long courseId);
    Optional<CourseFile> findByUrl(String url);
}