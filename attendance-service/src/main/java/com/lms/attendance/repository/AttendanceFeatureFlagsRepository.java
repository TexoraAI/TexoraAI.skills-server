package com.lms.attendance.repository;

import com.lms.attendance.entity.AttendanceFeatureFlags;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttendanceFeatureFlagsRepository extends JpaRepository<AttendanceFeatureFlags, String> {
    Optional<AttendanceFeatureFlags> findByScopeKey(String scopeKey);
}