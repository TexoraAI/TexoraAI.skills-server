package com.lms.chat.repository;

import com.lms.chat.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // ── Student queries ────────────────────────────────────────────
    List<Feedback> findByStudentEmailOrderByCreatedAtDesc(String studentEmail);

    List<Feedback> findByStudentEmailAndBatchIdOrderByCreatedAtDesc(
            String studentEmail, Long batchId);

    boolean existsByStudentEmailAndBatchId(String studentEmail, Long batchId);

    // ── Trainer queries ────────────────────────────────────────────
    List<Feedback> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    List<Feedback> findByTrainerEmailAndBatchIdOrderByCreatedAtDesc(
            String trainerEmail, Long batchId);

    // ── Batch / Admin queries ──────────────────────────────────────
    List<Feedback> findByBatchIdOrderByCreatedAtDesc(Long batchId);

    // ── Summary aggregates ─────────────────────────────────────────
    @Query("""
        SELECT COUNT(f) FROM Feedback f
        WHERE f.trainerEmail = :trainerEmail AND f.batchId = :batchId
    """)
    long countByTrainerAndBatch(
            @Param("trainerEmail") String trainerEmail,
            @Param("batchId") Long batchId);

    @Query("""
        SELECT AVG(CASE f.moodRating
            WHEN 'POOR'    THEN 1
            WHEN 'OKAY'    THEN 2
            WHEN 'FINE'    THEN 3
            WHEN 'GOOD'    THEN 4
            WHEN 'AMAZING' THEN 5
            ELSE 0 END)
        FROM Feedback f
        WHERE f.trainerEmail = :trainerEmail AND f.batchId = :batchId
    """)
    Double avgMoodScoreByTrainerAndBatch(
            @Param("trainerEmail") String trainerEmail,
            @Param("batchId") Long batchId);

    @Query("""
        SELECT AVG(f.trainerClarityRating) FROM Feedback f
        WHERE f.trainerEmail = :trainerEmail AND f.batchId = :batchId
          AND f.trainerClarityRating IS NOT NULL
    """)
    Double avgClarityByTrainerAndBatch(
            @Param("trainerEmail") String trainerEmail,
            @Param("batchId") Long batchId);

    @Query("""
        SELECT AVG(f.trainerDoubtClearingRating) FROM Feedback f
        WHERE f.trainerEmail = :trainerEmail AND f.batchId = :batchId
          AND f.trainerDoubtClearingRating IS NOT NULL
    """)
    Double avgDoubtClearingByTrainerAndBatch(
            @Param("trainerEmail") String trainerEmail,
            @Param("batchId") Long batchId);

    @Query("""
        SELECT AVG(f.trainerEnergyRating) FROM Feedback f
        WHERE f.trainerEmail = :trainerEmail AND f.batchId = :batchId
          AND f.trainerEnergyRating IS NOT NULL
    """)
    Double avgEnergyByTrainerAndBatch(
            @Param("trainerEmail") String trainerEmail,
            @Param("batchId") Long batchId);

    @Query("""
        SELECT AVG(f.trainerTechnicalDepthRating) FROM Feedback f
        WHERE f.trainerEmail = :trainerEmail AND f.batchId = :batchId
          AND f.trainerTechnicalDepthRating IS NOT NULL
    """)
    Double avgTechnicalDepthByTrainerAndBatch(
            @Param("trainerEmail") String trainerEmail,
            @Param("batchId") Long batchId);
}