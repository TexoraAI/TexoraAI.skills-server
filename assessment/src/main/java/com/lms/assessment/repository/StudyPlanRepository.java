//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.StudyPlan;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
//
//    List<StudyPlan> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);
//
//    List<StudyPlan> findByBatchIdAndActiveOrderByCreatedAtDesc(Long batchId, boolean active);
//
//    Optional<StudyPlan> findByIdAndTrainerEmail(Long id, String trainerEmail);
//
//    boolean existsByIdAndBatchId(Long id, Long batchId);
//
//    long countByTrainerEmail(String trainerEmail);
//}

package com.lms.assessment.repository;

import com.lms.assessment.model.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {

    List<StudyPlan> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    List<StudyPlan> findByBatchIdAndActiveOrderByCreatedAtDesc(Long batchId, boolean active);

    Optional<StudyPlan> findByIdAndTrainerEmail(Long id, String trainerEmail);

    boolean existsByIdAndBatchId(Long id, Long batchId);

    long countByTrainerEmail(String trainerEmail);

    // 🏢 Multi-tenancy — org-scoped finders
    List<StudyPlan> findByOrganizationIdAndTrainerEmailOrderByCreatedAtDesc(String organizationId, String trainerEmail);

    List<StudyPlan> findByOrganizationIdAndBatchIdAndActiveOrderByCreatedAtDesc(String organizationId, Long batchId, boolean active);

    Optional<StudyPlan> findByIdAndOrganizationIdAndTrainerEmail(Long id, String organizationId, String trainerEmail);

    Optional<StudyPlan> findByIdAndOrganizationId(Long id, String organizationId);

    boolean existsByIdAndOrganizationId(Long id, String organizationId);
    
    List<StudyPlan> findByOrganizationId(String organizationId);
    List<StudyPlan> findByOrganizationIdIsNull();
}