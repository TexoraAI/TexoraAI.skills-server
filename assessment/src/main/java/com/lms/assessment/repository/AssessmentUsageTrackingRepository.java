package com.lms.assessment.repository;

import com.lms.assessment.model.AssessmentUsageTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssessmentUsageTrackingRepository extends JpaRepository<AssessmentUsageTracking, Long> {

    Optional<AssessmentUsageTracking> findByEmailAndPeriodAndAction(
            String email, String period, String action);
}