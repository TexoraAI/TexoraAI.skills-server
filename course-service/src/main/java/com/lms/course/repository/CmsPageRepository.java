package com.lms.course.repository;

import com.lms.course.model.CmsPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link CmsPage}. Pages are looked up by their unique
 * pageKey (e.g. "student-hub") rather than by numeric id in most flows.
 */
public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {

    Optional<CmsPage> findByPageKey(String pageKey);

    boolean existsByPageKey(String pageKey);
}