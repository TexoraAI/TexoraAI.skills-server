package com.lms.progress.repository;

import com.lms.progress.model.RoadmapUsageTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoadmapUsageTrackingRepository extends JpaRepository<RoadmapUsageTracking, Long> {
    Optional<RoadmapUsageTracking> findByOwnerIdAndPeriod(Long ownerId, String period);
}