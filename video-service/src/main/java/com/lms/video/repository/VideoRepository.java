////package com.lms.video.repository;
////
////import com.lms.video.model.Video;
////import org.springframework.data.jpa.repository.JpaRepository;
////
////public interface VideoRepository extends JpaRepository<Video, Long> {
////}
//
//package com.lms.video.repository;
//import java.util.List;
//import com.lms.video.model.Video;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//import java.util.Optional;
//
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//public interface VideoRepository extends JpaRepository<Video, Long> {
//	List<Video> findByBatchId(Long batchId);
//	void deleteByBatchId(Long batchId);
//	List<Video> findByBatchIdIn(List<Long> batchIds);
//	List<Video> findByUploadedBy(String uploadedBy);
//
//	
//	List<Video> findByBatchIdInAndBatchIdIsNotNull(List<Long> batchIds);
//	// Add this line — that's it
//	List<Video> findByBatchIdInAndStatus(List<Long> batchIds, String status);
//	
//	
//	// For listVideos(): org admins should see only their org; super admins (org=null) keep seeing everything
//	List<Video> findByOrganizationIdOrderByUploadedAtDesc(String organizationId);
//
//	// For playVideo(): the endpoint currently has NO db lookup at all — it can't enforce
//	// anything because it doesn't know which Video a filename belongs to.
//	Optional<Video> findByStoredFileName(String storedFileName);
//
//	// For getVideosForStudent(): null-safe so the SAME method works for org students
//	// and (theoretically) non-org students without branching logic.
//	@Query("SELECT v FROM Video v WHERE v.batchId IN :batchIds AND v.status = :status " +
//	       "AND (:orgId IS NULL OR v.organizationId = :orgId)")
//	List<Video> findByBatchIdInAndStatusAndOrganizationId(
//	        @Param("batchIds") List<Long> batchIds,
//	        @Param("status") String status,
//	        @Param("orgId") String orgId);
//
//	// For getVideosForTrainer(): see the edge case in §5 — email is NOT guaranteed
//	// globally unique across organizations, so don't rely on uploadedBy alone.
//	@Query("SELECT v FROM Video v WHERE v.uploadedBy = :email AND (:orgId IS NULL OR v.organizationId = :orgId)")
//	List<Video> findByUploadedByAndOrganizationId(@Param("email") String email, @Param("orgId") String orgId);
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
	@Query("SELECT v FROM Video v WHERE v.batchId IN :batchIds AND v.status = :status " +
	       "AND (:orgId IS NULL OR v.organizationId = :orgId)")
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
}