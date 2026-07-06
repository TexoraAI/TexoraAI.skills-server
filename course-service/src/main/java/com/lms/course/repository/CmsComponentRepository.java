package com.lms.course.repository;

import com.lms.course.model.CmsComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link CmsComponent}. Supports single-section ordered
 * lookups as well as a batch lookup across multiple section ids (used when
 * building a full page tree in one pass to avoid N+1 queries).
 */
public interface CmsComponentRepository extends JpaRepository<CmsComponent, Long> {

    List<CmsComponent> findBySectionIdOrderByOrderIndexAsc(Long sectionId);

    List<CmsComponent> findBySectionIdIn(List<Long> sectionIds);
}