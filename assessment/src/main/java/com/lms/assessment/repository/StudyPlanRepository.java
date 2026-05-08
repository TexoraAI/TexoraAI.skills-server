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
}