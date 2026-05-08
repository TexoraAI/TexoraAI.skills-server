package com.lms.assessment.repository;

import com.lms.assessment.model.StudyPlanProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanProgressRepository extends JpaRepository<StudyPlanProgress, Long> {

    Optional<StudyPlanProgress> findByStudyPlanItemIdAndStudentEmail(
            Long studyPlanItemId, String studentEmail);

    List<StudyPlanProgress> findByStudyPlanIdAndStudentEmail(
            Long studyPlanId, String studentEmail);

    long countByStudyPlanIdAndStudentEmailAndCompleted(
            Long studyPlanId, String studentEmail, boolean completed);

    boolean existsByStudyPlanItemIdAndStudentEmailAndCompleted(
            Long studyPlanItemId, String studentEmail, boolean completed);

    @Query("""
        SELECT COUNT(DISTINCT p.studentEmail)
        FROM StudyPlanProgress p
        WHERE p.studyPlanId = :planId
    """)
    long countDistinctStudentsByPlan(@Param("planId") Long planId);
}