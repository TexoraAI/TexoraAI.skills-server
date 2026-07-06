package com.lms.course.repository;

import com.lms.course.model.CmsSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link CmsSection}. Sections are always fetched ordered
 * by their drag/reorder position within a page.
 */
public interface CmsSectionRepository extends JpaRepository<CmsSection, Long> {

    List<CmsSection> findByPageIdOrderByOrderIndexAsc(Long pageId);
}