package com.lms.course.repository;

import com.lms.course.model.CmsMediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for {@link CmsMediaAsset}. Media is scoped globally (not per
 * page) and supports a simple name-based search for the media library UI.
 */
public interface CmsMediaAssetRepository extends JpaRepository<CmsMediaAsset, Long> {

    List<CmsMediaAsset> findByOriginalFileNameContainingIgnoreCase(String search);
}