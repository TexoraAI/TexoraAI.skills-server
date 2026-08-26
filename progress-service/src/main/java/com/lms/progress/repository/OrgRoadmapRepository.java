package com.lms.progress.repository;

import com.lms.progress.model.OrgRoadmap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrgRoadmapRepository extends JpaRepository<OrgRoadmap, Long> {

    Optional<OrgRoadmap> findBySlugAndOrgId(String slug, String orgId);

    Optional<OrgRoadmap> findBySlugAndOrgIdIsNull(String slug);

    boolean existsBySlugAndOrgId(String slug, String orgId);

    boolean existsBySlugAndOrgIdIsNull(String slug);

    Page<OrgRoadmap> findByOrgIdAndIsPublishedTrueAndIsArchivedFalse(String orgId, Pageable pageable);

    Page<OrgRoadmap> findByOrgIdIsNullAndIsPublishedTrueAndIsArchivedFalse(Pageable pageable);

    Page<OrgRoadmap> findByCreatedBy(Long createdBy, Pageable pageable);
}