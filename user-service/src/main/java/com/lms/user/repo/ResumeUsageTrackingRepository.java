package com.lms.user.repo;

import com.lms.user.model.ResumeUsageTracking;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeUsageTrackingRepository
    extends JpaRepository<ResumeUsageTracking, Long> {

    Optional<ResumeUsageTracking> findByUserIdAndPeriod(Long userId, String period);
}