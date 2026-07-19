//package com.lms.live_session.repository;
//
//import com.lms.live_session.entity.LiveSession;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
//
//    List<LiveSession> findByBatchId(Long batchId);
//
//    List<LiveSession> findByBatchIdIn(List<Long> batchIds);
//
//    void deleteByBatchId(Long batchId);
//
//    List<LiveSession> findByBatchIdAndStatus(Long batchId, String status);
//
//    List<LiveSession> findByStatus(String status);
//
//    List<LiveSession> findByStatusAndScheduledDate(String status, LocalDate scheduledDate);
//
//    // ✅ Trainer-specific queries
//    List<LiveSession> findByTrainerEmailOrderByScheduledDateDesc(String trainerEmail);
//
//    List<LiveSession> findByTrainerEmailAndStatus(String trainerEmail, String status);
//
//    List<LiveSession> findByTrainerEmailAndBatchId(String trainerEmail, Long batchId);
//    
//    List<LiveSession> findByStatusIn(List<String> statuses);
//    
//    
// // ADD these 2 methods to LiveSessionRepository
//
// // For calendar: trainer's sessions by date range
// List<LiveSession> findByTrainerEmailAndScheduledDateBetween(
//     String trainerEmail, LocalDate start, LocalDate end);
//
// // For global published sessions (no batchId filter)
// List<LiveSession> findByIsPublishedTrueAndStatusIn(List<String> statuses);
//}
//
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

    // ─────────────────────────────────────────────────────────────
    // ✅ NEW — atomic, DB-enforced guards. Each is a single UPDATE
    // statement with a conditional WHERE clause, so the "check" and
    // the "write" happen as one atomic operation instead of two
    // separate round-trips that concurrent threads can race through.
    // ─────────────────────────────────────────────────────────────

    /**
     * Flips SCHEDULED -> LIVE exactly once. Returns 1 if THIS call won the
     * race and performed the transition, 0 if the session was already LIVE
     * (i.e. someone else got there first).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE LiveSession s SET s.status = 'LIVE', s.actualStartTime = :now " +
           "WHERE s.id = :id AND s.status = 'SCHEDULED'")
    int atomicMarkLive(@Param("id") Long id, @Param("now") LocalDateTime now);

    /**
     * Claims the "right to start an egress" by writing a claim token into
     * egressId, but ONLY if egressId is currently null. Returns 1 if this
     * call claimed the slot, 0 if something else already holds it.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE LiveSession s SET s.egressId = :claimToken " +
           "WHERE s.id = :id AND s.egressId IS NULL")
    int atomicClaimEgressSlot(@Param("id") Long id, @Param("claimToken") String claimToken);

    /**
     * Swaps a claim token for the real LiveKit egressId, once egress has
     * actually started. Only succeeds if the claim token is still in place
     * (i.e. nobody else touched the row in the meantime).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE LiveSession s SET s.egressId = :newEgressId " +
           "WHERE s.id = :id AND s.egressId = :claimToken")
    int atomicFinalizeEgressId(@Param("id") Long id,
                               @Param("claimToken") String claimToken,
                               @Param("newEgressId") String newEgressId);

    /**
     * Clears egressId, but ONLY if it still equals the value the caller
     * expects. Used both to release a claim slot after a failed egress
     * start, and to clear egressId after a legitimate stop — without
     * clobbering a different egressId that might have been written since.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE LiveSession s SET s.egressId = NULL " +
           "WHERE s.id = :id AND s.egressId = :expectedEgressId")
    int atomicClearEgressId(@Param("id") Long id, @Param("expectedEgressId") String expectedEgressId);
}