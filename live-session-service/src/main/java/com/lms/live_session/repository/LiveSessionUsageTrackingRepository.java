package com.lms.live_session.repository;

import com.lms.live_session.entity.LiveSessionUsageTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LiveSessionUsageTrackingRepository extends JpaRepository<LiveSessionUsageTracking, Long> {

    Optional<LiveSessionUsageTracking> findByEmailAndPeriodAndAction(String email, String period, String action);
}
