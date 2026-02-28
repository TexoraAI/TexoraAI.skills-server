package com.lms.course.repository;

import com.lms.course.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByOwnerEmail(String ownerEmail);
    
    List<Course> findByBatchId(Long batchId);
    List<Course> findByBatchIdAndOwnerEmail(Long batchId, String ownerEmail);
    void deleteByBatchId(Long batchId);
    
    List<Course> findByBatchIdIn(List<Long> batchIds);
    List<Course> findAllByOrderByCreatedAtDesc();
    
    List<Course> findByCategoryIgnoreCase(String category);
}
