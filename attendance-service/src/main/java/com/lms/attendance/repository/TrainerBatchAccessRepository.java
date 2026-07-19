//
//
//package com.lms.attendance.repository;
//
//import com.lms.attendance.entity.TrainerBatchAccess;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface TrainerBatchAccessRepository
//        extends JpaRepository<TrainerBatchAccess, Long> {
//
//    Optional<TrainerBatchAccess> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);
//
//    // NEW — used when the authenticated trainer belongs to an organization
//    Optional<TrainerBatchAccess> findByBatchIdAndTrainerEmailAndOrganizationId(
//            Long batchId, String trainerEmail, String organizationId);
//
//    void deleteByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);
//
//    void deleteByBatchId(Long batchId);
//}
package com.lms.attendance.repository;

import com.lms.attendance.entity.TrainerBatchAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainerBatchAccessRepository
        extends JpaRepository<TrainerBatchAccess, Long> {

    Optional<TrainerBatchAccess> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);

    // NEW — used when the authenticated trainer belongs to an organization
    Optional<TrainerBatchAccess> findByBatchIdAndTrainerEmailAndOrganizationId(
            Long batchId, String trainerEmail, String organizationId);

    void deleteByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);

    void deleteByBatchId(Long batchId);

    // ── NEW: used by admin/superadmin attendance overview endpoints ──

    // Admin overview: all distinct batches whose trainer belongs to this org
    @Query("SELECT DISTINCT t.batchId FROM TrainerBatchAccess t WHERE t.organizationId = :organizationId")
    List<Long> findDistinctBatchIdsByOrganizationId(@Param("organizationId") String organizationId);

    // Super Admin overview: all distinct batches whose trainer is orgless (standalone)
    @Query("SELECT DISTINCT t.batchId FROM TrainerBatchAccess t WHERE t.organizationId IS NULL")
    List<Long> findDistinctBatchIdsByOrganizationIdIsNull();

    // Used to resolve which trainer(s) own a given batch, for org-match checks on the
    // admin/{batchId} and superadmin/{batchId} endpoints
    List<TrainerBatchAccess> findByBatchId(Long batchId);
}