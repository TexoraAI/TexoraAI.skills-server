package com.lms.progress.repository;

import com.lms.progress.model.RoadmapTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoadmapTemplateRepository extends JpaRepository<RoadmapTemplate, Long> {

    Optional<RoadmapTemplate> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<RoadmapTemplate> findByIsPublishedTrueAndIsArchivedFalse(Pageable pageable);
}
