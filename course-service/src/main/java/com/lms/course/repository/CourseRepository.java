package com.lms.course.repository;

import com.lms.course.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import org.springframework.data.repository.query.Param;
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByOwnerEmail(String ownerEmail);
    
    List<Course> findByBatchId(Long batchId);
    List<Course> findByBatchIdAndOwnerEmail(Long batchId, String ownerEmail);
    void deleteByBatchId(Long batchId);
    
    List<Course> findByBatchIdIn(List<Long> batchIds);
    List<Course> findAllByOrderByCreatedAtDesc();
    
    List<Course> findByCategoryIgnoreCase(String category);
    
 // find all courses for an org
    List<Course> findByOrganizationId(String organizationId);

    // all distinct categories across all courses
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.category IS NOT NULL ORDER BY c.category")
    List<String> findAllDistinctCategories();

    // distinct categories for one org
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.organizationId = :orgId AND c.category IS NOT NULL ORDER BY c.category")
    List<String> findDistinctCategoriesByOrganizationId(@Param("orgId") String orgId);
    
    List<Course> findByOrganizationIdIsNull();
    @Query("SELECT DISTINCT c.category FROM Course c WHERE c.organizationId IS NULL AND c.category IS NOT NULL ORDER BY c.category")
    List<String> findDistinctCategoryByOrganizationIdIsNull();
//    List<String> findDistinctCategoryByOrganizationIdIsNull();
    
 // Trainer sees courses assigned to them by admin
    List<Course> findByAssignedTrainerEmailAndOrganizationId(
            String assignedTrainerEmail, String organizationId);

    // Admin clicks trainer email → all courses in org belonging to that trainer
    List<Course> findByOrganizationIdAndAssignedTrainerEmail(
            String organizationId, String assignedTrainerEmail);
  

   
}
