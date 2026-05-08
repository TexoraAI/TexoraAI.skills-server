package com.lms.progress.repository;

import com.lms.progress.model.SkillScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkillScoreRepository extends JpaRepository<SkillScore, Long> {

    // ── Student queries ──

    // All skills for one student in one batch
    List<SkillScore> findByStudentEmailAndBatchId(String studentEmail, Long batchId);

    // All skills for one student across ALL batches
    List<SkillScore> findByStudentEmail(String studentEmail);

    // Single skill record (used for upsert)
    Optional<SkillScore> findByStudentEmailAndBatchIdAndSkillName(
            String studentEmail, Long batchId, String skillName);

    // ── Trainer queries ──

    // All skill records for a batch (all students in that batch)
    List<SkillScore> findByBatchId(Long batchId);

    // Trainer's own batches
    List<SkillScore> findByTrainerEmail(String trainerEmail);

    // Weak students in a batch
    List<SkillScore> findByBatchIdAndIsWeak(Long batchId, boolean isWeak);

    // ── Admin queries ──

    // All skill records (org-wide) — used for admin dashboard
    // We group in service layer to avoid huge native queries
    @Query("SELECT DISTINCT s.studentEmail FROM SkillScore s")
    List<String> findAllStudentEmails();

    @Query("SELECT DISTINCT s.batchId FROM SkillScore s")
    List<Long> findAllBatchIds();

    // Average score per skill name across entire org
    @Query("SELECT s.skillName, AVG(s.overallScore) FROM SkillScore s GROUP BY s.skillName")
    List<Object[]> findOrgSkillAverages();

    // Average score per skill per batch (for by-batch tab)
    @Query("SELECT s.batchId, s.skillName, AVG(s.overallScore) " +
           "FROM SkillScore s GROUP BY s.batchId, s.skillName")
    List<Object[]> findBatchSkillAverages();
}