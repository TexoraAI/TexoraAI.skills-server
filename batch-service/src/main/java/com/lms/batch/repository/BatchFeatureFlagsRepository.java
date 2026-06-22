package com.lms.batch.repository;

import com.lms.batch.entity.BatchFeatureFlags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatchFeatureFlagsRepository extends JpaRepository<BatchFeatureFlags, String> {
    Optional<BatchFeatureFlags> findByScopeKey(String scopeKey);
}