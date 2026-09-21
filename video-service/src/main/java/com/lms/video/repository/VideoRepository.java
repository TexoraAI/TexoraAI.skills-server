//
//package com.lms.video.repository;
//
//import java.util.List;
//import java.util.Optional;
//
//import com.lms.video.model.Video;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//public interface VideoRepository extends JpaRepository<Video, Long> {
//
//	List<Video> findByBatchId(Long batchId);
//	void deleteByBatchId(Long batchId);
//	List<Video> findByBatchIdIn(List<Long> batchIds);
//	List<Video> findByUploadedBy(String uploadedBy);
//
//	List<Video> findByBatchIdInAndBatchIdIsNotNull(List<Long> batchIds);
//	List<Video> findByBatchIdInAndStatus(List<Long> batchIds, String status);
//
//	// ✅ NEW — admin listing, scoped to the caller's organization.
//	// Used when organizationId != null. Non-org callers keep using findAll().
//	List<Video> findByOrganizationIdOrderByUploadedAtDesc(String organizationId);
//
//	// ✅ NEW — needed because /play/{fileName} previously had NO db lookup at
//	// all, so there was nothing to enforce an org check against.
//	Optional<Video> findByStoredFileName(String storedFileName);
//
//	// ✅ NEW — null-safe org filter for the student dashboard.
//	// A plain derived method (column = ?) would never match NULL for
//	// non-org callers, so this is an explicit JPQL query instead.
//	@Query("SELECT v FROM Video v WHERE v.batchId IN :batchIds AND v.status = :status " +
//	       "AND (:orgId IS NULL OR v.organizationId = :orgId)")
//	List<Video> findByBatchIdInAndStatusAndOrganizationId(
//			@Param("batchIds") List<Long> batchIds,
//			@Param("status") String status,
//			@Param("orgId") String orgId);
//
//	// ✅ NEW — null-safe org filter for the trainer dashboard.
//	// Filtering by email alone is not safe across orgs if email uniqueness
//	// is not guaranteed globally (see risk notes).
//	@Query("SELECT v FROM Video v WHERE v.uploadedBy = :email " +
//	       "AND (:orgId IS NULL OR v.organizationId = :orgId)")
//	List<Video> findByUploadedByAndOrganizationId(
//			@Param("email") String email,
//			@Param("orgId") String orgId);
//}


package com.lms.video.repository;

import java.util.List;
import java.util.Optional;

import com.lms.video.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoRepository extends JpaRepository<Video, Long> {

	List<Video> findByBatchId(Long batchId);
	void deleteByBatchId(Long batchId);
	List<Video> findByBatchIdIn(List<Long> batchIds);
	List<Video> findByUploadedBy(String uploadedBy);

	List<Video> findByBatchIdInAndBatchIdIsNotNull(List<Long> batchIds);
	List<Video> findByBatchIdInAndStatus(List<Long> batchIds, String status);

	// ✅ NEW — admin listing, scoped to the caller's organization.
	// Used when organizationId != null. Non-org callers keep using findAll().
	List<Video> findByOrganizationIdOrderByUploadedAtDesc(String organizationId);

	// ✅ NEW — needed because /play/{fileName} previously had NO db lookup at
	// all, so there was nothing to enforce an org check against.
	Optional<Video> findByStoredFileName(String storedFileName);

	// ✅ NEW — null-safe org filter for the student dashboard.
	// A plain derived method (column = ?) would never match NULL for
	// non-org callers, so this is an explicit JPQL query instead.
	// ✅ CHANGED — explicit ORDER BY uploaded_at DESC added so the
	// student-visible-cap truncation (step 6 of the tier-limits task)
	// truncates to the MOST RECENT videos, not an arbitrary DB order.
	@Query("SELECT v FROM Video v WHERE v.batchId IN :batchIds AND v.status = :status " +
	       "AND (:orgId IS NULL OR v.organizationId = :orgId) " +
	       "ORDER BY v.uploadedAt DESC")
	List<Video> findByBatchIdInAndStatusAndOrganizationId(
			@Param("batchIds") List<Long> batchIds,
			@Param("status") String status,
			@Param("orgId") String orgId);

	// ✅ NEW — null-safe org filter for the trainer dashboard.
	// Filtering by email alone is not safe across orgs if email uniqueness
	// is not guaranteed globally (see risk notes).
	@Query("SELECT v FROM Video v WHERE v.uploadedBy = :email " +
	       "AND (:orgId IS NULL OR v.organizationId = :orgId)")
	List<Video> findByUploadedByAndOrganizationId(
			@Param("email") String email,
			@Param("orgId") String orgId);

	// ✅ NEW — plan-tier quota enforcement (upload-limits task).
	// Shared-pool-per-org / per-person-if-standalone rule: when orgId is
	// present, usage is summed across the WHOLE org (all trainers share one
	// quota); when orgId is null (standalone trainer), usage is scoped to
	// just that trainer's own uploads.
	@Query("SELECT COALESCE(SUM(v.size), 0) FROM Video v WHERE " +
	       "(:orgId IS NOT NULL AND v.organizationId = :orgId) OR " +
	       "(:orgId IS NULL AND v.uploadedBy = :email AND v.organizationId IS NULL)")
	long sumStorageUsage(@Param("orgId") String orgId, @Param("email") String email);

	// ✅ NEW — plan-tier quota enforcement (upload-limits task). Same
	// shared-pool-per-org / per-person-if-standalone scoping as above.
	@Query("SELECT COUNT(v) FROM Video v WHERE " +
	       "(:orgId IS NOT NULL AND v.organizationId = :orgId) OR " +
	       "(:orgId IS NULL AND v.uploadedBy = :email AND v.organizationId IS NULL)")
	long countVideos(@Param("orgId") String orgId, @Param("email") String email);
}