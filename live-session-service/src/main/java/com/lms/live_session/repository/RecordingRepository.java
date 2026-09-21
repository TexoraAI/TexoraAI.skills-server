package com.lms.live_session.repository;
import com.lms.live_session.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<Recording> findByBatchId(Long batchId);
    List<Recording> findBySessionId(Long sessionId);
    List<Recording> findByTrainerEmail(String trainerEmail);
    List<Recording> findByTrainerEmailAndBatchId(String trainerEmail, Long batchId);
    List<Recording> findByStatus(String status);
    List<Recording> findByBatchIdAndStatus(Long batchId, String status);
    List<Recording> findByRecordingType(String recordingType);
    boolean existsBySessionId(Long sessionId);
    @Modifying
    @Transactional
    @Query("UPDATE Recording r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    void incrementViewCount(@Param("id") Long id);
    List<Recording> findByBatchIdAndStatusOrderByCreatedAtDesc(Long batchId, String status);
    List<Recording> findAllByOrderByCreatedAtDesc();
    List<Recording> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);

    // ✅ NEW — org-aware variants. Rule: if :orgId is null (non-org caller), no
    // restriction at all. If :orgId is present, only rows with a NULL org
    // (legacy/untagged, pre-multi-tenancy) or a matching org are visible —
    // same "null = unrestricted" convention used in TrainerBatchMap/LiveSession.

    @Query("SELECT r FROM Recording r WHERE r.batchId = :batchId AND r.status = :status " +
           "AND (:orgId IS NULL OR r.organizationId IS NULL OR r.organizationId = :orgId) " +
           "ORDER BY r.createdAt DESC")
    List<Recording> findByBatchIdAndStatusForOrg(
        @Param("batchId") Long batchId, @Param("status") String status, @Param("orgId") Long orgId);

    @Query("SELECT r FROM Recording r WHERE r.sessionId = :sessionId " +
           "AND (:orgId IS NULL OR r.organizationId IS NULL OR r.organizationId = :orgId)")
    List<Recording> findBySessionIdForOrg(
        @Param("sessionId") Long sessionId, @Param("orgId") Long orgId);

    @Query("SELECT r FROM Recording r WHERE (:orgId IS NULL OR r.organizationId IS NULL OR r.organizationId = :orgId) " +
           "ORDER BY r.createdAt DESC")
    List<Recording> findAllForOrgOrderByCreatedAtDesc(@Param("orgId") Long orgId);

    // ✅ NEW — plan-tier storage enforcement (LiveSessionUsageService.checkRecordingLimits).
    // Sums bytes across ALL of a trainer's recordings regardless of org/status,
    // matching the spec's simple per-trainer storage cap check.
    @Query("SELECT COALESCE(SUM(r.fileSizeBytes), 0) FROM Recording r WHERE r.trainerEmail = :email")
    long sumFileSizeByTrainer(@Param("email") String email);
}