//package com.lms.live_session.repository;
//
//import com.lms.live_session.entity.SessionParticipant;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//
//public interface ParticipantRepository extends JpaRepository<SessionParticipant, Long> {
//
//    List<SessionParticipant> findBySessionId(Long sessionId);
//
//}
// repository/ParticipantRepository.java
package com.lms.live_session.repository;

import com.lms.live_session.entity.SessionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<SessionParticipant, Long> {

    // ── Session queries ────────────────────────────────────────
    List<SessionParticipant> findBySessionId(Long sessionId);

    // ── Student queries ────────────────────────────────────────
    List<SessionParticipant> findByStudentEmailOrderByJoinTimeDesc(String studentEmail);

    List<SessionParticipant> findByStudentEmailAndBatchId(String studentEmail, Long batchId);

    // ── Check if already joined and still active ───────────────
    boolean existsBySessionIdAndStudentEmailAndLeaveTimeIsNull(
        Long sessionId, String studentEmail
    );

    // ── Find active participant record ─────────────────────────
    Optional<SessionParticipant> findBySessionIdAndStudentEmailAndLeaveTimeIsNull(
        Long sessionId, String studentEmail
    );

    // ── Trainer queries ────────────────────────────────────────
    List<SessionParticipant> findByTrainerEmail(String trainerEmail);

    List<SessionParticipant> findByTrainerEmailAndBatchId(String trainerEmail, Long batchId);

    // ── Batch queries ──────────────────────────────────────────
    List<SessionParticipant> findByBatchId(Long batchId);

    // ── Count active participants in a session ─────────────────
    @Query("""
        SELECT COUNT(p) FROM SessionParticipant p
        WHERE p.sessionId = :sessionId AND p.leaveTime IS NULL
    """)
    long countActiveBySession(@Param("sessionId") Long sessionId);

    // ── Average watch percentage for a session ─────────────────
    @Query("""
        SELECT AVG(p.watchPercentage) FROM SessionParticipant p
        WHERE p.sessionId = :sessionId AND p.watchPercentage IS NOT NULL
    """)
    Double avgWatchPercentageBySession(@Param("sessionId") Long sessionId);

    // ── All participants for a session with their status ───────
    @Query("""
        SELECT p FROM SessionParticipant p
        WHERE p.sessionId = :sessionId
        ORDER BY p.joinTime DESC
    """)
    List<SessionParticipant> findBySessionIdOrderByJoinTimeDesc(
        @Param("sessionId") Long sessionId
    );
}