package com.lms.assessment.repository;

import com.lms.assessment.model.AssessmentFeatureFlags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssessmentFeatureFlagsRepository extends JpaRepository<AssessmentFeatureFlags, String> {
    Optional<AssessmentFeatureFlags> findByScopeKey(String scopeKey);
}