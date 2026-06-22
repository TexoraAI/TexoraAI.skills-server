//package com.lms.course.repository;
//import java.util.List;
//import org.springframework.data.jpa.repository.JpaRepository;
//import com.lms.course.model.TrainerBatchMap;
//
//public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {
//
//    List<TrainerBatchMap> findByTrainerEmail(String email);
//    void deleteByTrainerEmailAndBatchId(String email, Long batchId);
//    void deleteByBatchId(Long batchId);
//    boolean existsByTrainerEmailAndBatchId(String email, Long batchId);
//}

package com.lms.course.repository;

import com.lms.course.model.TrainerBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {

    // Existing — used everywhere, do NOT remove
    boolean existsByTrainerEmailAndBatchId(String trainerEmail, Long batchId);

    // Existing — used in CourseService.getTrainerCourses()
    List<TrainerBatchMap> findByTrainerEmail(String trainerEmail);

    // Existing — used in BatchAssignmentConsumer remove logic
    void deleteByTrainerEmailAndBatchId(String trainerEmail, Long batchId);

    // NEW — used in CourseService.create() for org-based trainer validation
    // Ensures the trainer is assigned to the batch AND that batch belongs to their org
    boolean existsByTrainerEmailAndBatchIdAndOrganizationId(
            String trainerEmail, Long batchId, String organizationId);
}