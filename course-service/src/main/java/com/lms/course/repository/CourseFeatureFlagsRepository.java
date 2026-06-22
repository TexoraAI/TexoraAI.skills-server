package com.lms.course.repository;

import com.lms.course.model.CourseFeatureFlags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseFeatureFlagsRepository extends JpaRepository<CourseFeatureFlags, String> {
    Optional<CourseFeatureFlags> findByScopeKey(String scopeKey);
}