package com.lms.file.repository;

import com.lms.file.model.FileFeatureFlags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileFeatureFlagsRepository extends JpaRepository<FileFeatureFlags, String> {
    Optional<FileFeatureFlags> findByScopeKey(String scopeKey);
}