package com.lms.video.repository;

import com.lms.video.model.VideoFeatureFlags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VideoFeatureFlagsRepository extends JpaRepository<VideoFeatureFlags, String> {
    Optional<VideoFeatureFlags> findByScopeKey(String scopeKey);
}