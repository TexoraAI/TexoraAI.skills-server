package com.lms.progress.repository;

import com.lms.progress.model.RoadmapUpgradedSyllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoadmapUpgradedRepository extends JpaRepository<RoadmapUpgradedSyllabus, Long> {

    List<RoadmapUpgradedSyllabus> findByOwnerId(Long ownerId);

    List<RoadmapUpgradedSyllabus> findByOrganizationId(String organizationId);

    List<RoadmapUpgradedSyllabus> findByOrganizationIdIsNull();

    /**
     * First-ever click on a library targetRole: is there already a cached/
     * generated skeleton for this exact targetRole (any owner, sourceType
     * LIBRARY, status READY) that can be cloned instantly for a new user?
     */
    List<RoadmapUpgradedSyllabus> findFirstByTargetRoleAndSourceTypeAndStatus(
            String targetRole, String sourceType, String status);

    @Query("SELECT r.ownerId, COUNT(r) FROM RoadmapUpgradedSyllabus r " +
            "WHERE r.organizationId = :orgId AND r.ownerRole = :role " +
            "GROUP BY r.ownerId ORDER BY COUNT(r) DESC")
    List<Object[]> findTopUsersByOrgAndRole(@Param("orgId") String orgId, @Param("role") String role);

    @Query("SELECT r.ownerId, COUNT(r) FROM RoadmapUpgradedSyllabus r " +
            "WHERE r.organizationId IS NULL AND r.ownerRole = :role " +
            "GROUP BY r.ownerId ORDER BY COUNT(r) DESC")
    List<Object[]> findTopNullOrgUsersByRole(@Param("role") String role);

    @Query("SELECT DISTINCT r.organizationId FROM RoadmapUpgradedSyllabus r WHERE r.organizationId IS NOT NULL")
    List<String> findAllDistinctOrganizationIds();

    Long countByOrganizationId(String organizationId);

    Long countByOrganizationIdIsNull();

    Long countByOrganizationIdAndOwnerRole(String organizationId, String ownerRole);

    @Query("SELECT AVG(r.completionPercent) FROM RoadmapUpgradedSyllabus r WHERE r.ownerId = :ownerId")
    Double findAvgCompletionPercentByOwnerId(@Param("ownerId") Long ownerId);

    /**
     * Distinct-user count (not row count) of a given role within an org, for
     * "totalStudentsInOrg" / "totalTrainersInOrg" admin-stats fields. This
     * service only sees users through the roadmaps they've generated, so this
     * is a best-effort count scoped to that visibility (not a full org
     * roster - this service doesn't own that data).
     */
    @Query("SELECT COUNT(DISTINCT r.ownerId) FROM RoadmapUpgradedSyllabus r " +
            "WHERE r.organizationId = :orgId AND r.ownerRole = :role")
    Long countDistinctOwnersByOrganizationIdAndOwnerRole(@Param("orgId") String orgId, @Param("role") String role);

    @Query("SELECT r.pathType, COUNT(r) FROM RoadmapUpgradedSyllabus r " +
            "WHERE r.organizationId = :orgId GROUP BY r.pathType")
    List<Object[]> findPathTypeBreakdownByOrganizationId(@Param("orgId") String orgId);

    @Query("SELECT r.ownerId, r.ownerRole, r.organizationId, COUNT(r) FROM RoadmapUpgradedSyllabus r " +
            "GROUP BY r.ownerId, r.ownerRole, r.organizationId ORDER BY COUNT(r) DESC")
    List<Object[]> findTopUsersPlatformWide();

    /**
     * Finds the syllabus (aggregate root) that owns the resource with the
     * given id, so resource-level operations (mark complete) can be reached
     * without a separate resource repository - resources are a child entity
     * of the syllabus aggregate, not their own aggregate root.
     */
    @Query("SELECT DISTINCT r FROM RoadmapUpgradedSyllabus r " +
            "JOIN r.modules m JOIN m.topics t JOIN t.resources res WHERE res.id = :resourceId")
    Optional<RoadmapUpgradedSyllabus> findSyllabusByResourceId(@Param("resourceId") Long resourceId);
}
