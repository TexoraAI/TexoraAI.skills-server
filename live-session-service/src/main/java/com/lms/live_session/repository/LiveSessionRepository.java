package com.lms.live_session.repository;

import com.lms.live_session.entity.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
    List<LiveSession> findByBatchId(Long batchId);
    List<LiveSession> findByBatchIdIn(List<Long> batchIds);
    void deleteByBatchId(Long batchId);
    List<LiveSession> findByBatchIdAndStatus(Long batchId, String status);
    List<LiveSession> findByStatus(String status);
    List<LiveSession> findByStatusAndScheduledDate(String status, LocalDate scheduledDate);
    List<LiveSession> findByTrainerEmailOrderByScheduledDateDesc(String trainerEmail);
    List<LiveSession> findByTrainerEmailAndStatus(String trainerEmail, String status);
    List<LiveSession> findByTrainerEmailAndBatchId(String trainerEmail, Long batchId);
    List<LiveSession> findByStatusIn(List<String> statuses);
    List<LiveSession> findByTrainerEmailAndScheduledDateBetween(
        String trainerEmail, LocalDate start, LocalDate end);
    List<LiveSession> findByIsPublishedTrueAndStatusIn(List<String> statuses);

    // ✅ NEW — org-aware batch queries for Step 5 read filtering.
    // Same "null = unrestricted, org-null legacy rows stay visible" rule
    // used across TrainerBatchMap/Recording. Plain @Query (not @Modifying),
    // so no clearAutomatically/flushAutomatically needed — this is a read.
    @Query("SELECT s FROM LiveSession s WHERE s.batchId = :batchId " +
           "AND (:orgId IS NULL OR s.organizationId IS NULL OR s.organizationId = :orgId)")
    List<LiveSession> findByBatchIdForOrg(@Param("batchId") Long batchId, @Param("orgId") Long orgId);

    @Query("SELECT s FROM LiveSession s WHERE s.batchId = :batchId AND s.status = :status " +
           "AND (:orgId IS NULL OR s.organizationId IS NULL OR s.organizationId = :orgId)")
    List<LiveSession> findByBatchIdAndStatusForOrg(
        @Param("batchId") Long batchId, @Param("status") String status, @Param("orgId") Long orgId);

    // ─────────────────────────────────────────────────────────────
    // Atomic, DB-enforced guards — UNCHANGED
    // ─────────────────────────────────────────────────────────────

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE LiveSession s SET s.status = 'LIVE', s.actualStartTime = :now " +
           "WHERE s.id = :id AND s.status = 'SCHEDULED'")
    int atomicMarkLive(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE LiveSession s SET s.egressId = :claimToken " +
           "WHERE s.id = :id AND s.egressId IS NULL")
    int atomicClaimEgressSlot(@Param("id") Long id, @Param("claimToken") String claimToken);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE LiveSession s SET s.egressId = :newEgressId " +
           "WHERE s.id = :id AND s.egressId = :claimToken")
    int atomicFinalizeEgressId(@Param("id") Long id,
                               @Param("claimToken") String claimToken,
                               @Param("newEgressId") String newEgressId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE LiveSession s SET s.egressId = NULL " +
           "WHERE s.id = :id AND s.egressId = :expectedEgressId")
    int atomicClearEgressId(@Param("id") Long id, @Param("expectedEgressId") String expectedEgressId);
}