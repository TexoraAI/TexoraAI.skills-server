//package com.lms.file.repository;
//
//import com.lms.file.model.FileResource;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//public interface FileRepository extends JpaRepository<FileResource, Long> {
//	void deleteAllByBatchId(Long batchId);
//	List<FileResource> findByTrainerEmail(String trainerEmail);
//	List<FileResource> findByBatchId(Long batchId);
//	List<FileResource> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);
//	
//	
//	// ✅ ADD THESE TWO
//    List<FileResource> findByBatchIdInAndStatus(List<Long> batchIds, String status);
//    List<FileResource> findByBatchIdIn(List<Long> batchIds);
//}
//
package com.lms.file.repository;

import com.lms.file.model.FileResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface FileRepository extends JpaRepository<FileResource, Long> {
	void deleteAllByBatchId(Long batchId);
	List<FileResource> findByTrainerEmail(String trainerEmail);
	List<FileResource> findByBatchId(Long batchId);
	List<FileResource> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);
	
	
	// ✅ ADD THESE TWO
    List<FileResource> findByBatchIdInAndStatusOrderByUploadedAtDesc(List<Long> batchIds, String status);
    List<FileResource> findByBatchIdIn(List<Long> batchIds);

    // ✅ PLAN-TIER: org-wide (via BatchTrainer) or individual storage usage, in bytes
    @Query("SELECT COALESCE(SUM(f.size), 0) FROM FileResource f WHERE " +
           "(:orgId IS NOT NULL AND f.trainerEmail IN " +
           "  (SELECT bt.trainerEmail FROM BatchTrainer bt WHERE bt.organizationId = :orgId)) OR " +
           "(:orgId IS NULL AND f.trainerEmail = :email)")
    long sumStorageUsage(@Param("orgId") String orgId, @Param("email") String email);

    // ✅ PLAN-TIER: org-wide (via BatchTrainer) or individual file count
    @Query("SELECT COUNT(f) FROM FileResource f WHERE " +
           "(:orgId IS NOT NULL AND f.trainerEmail IN " +
           "  (SELECT bt.trainerEmail FROM BatchTrainer bt WHERE bt.organizationId = :orgId)) OR " +
           "(:orgId IS NULL AND f.trainerEmail = :email)")
    long countFiles(@Param("orgId") String orgId, @Param("email") String email);
}
