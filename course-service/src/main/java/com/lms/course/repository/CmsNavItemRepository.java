package com.lms.course.repository;

import com.lms.course.model.CmsNavItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link CmsNavItem}. Navigation lists are scoped per
 * pageKey (Student Hub, Trainer Hub, Admin Hub each have their own list).
 */
public interface CmsNavItemRepository extends JpaRepository<CmsNavItem, Long> {

    List<CmsNavItem> findByPageKeyOrderByOrderIndexAsc(String pageKey);
}