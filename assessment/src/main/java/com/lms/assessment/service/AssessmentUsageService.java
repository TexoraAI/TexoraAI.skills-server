package com.lms.assessment.service;

import com.lms.assessment.constants.AssessmentTierResolver;
import com.lms.assessment.constants.AssessmentUsageLimits;
import com.lms.assessment.exception.AssessmentUsageLimitExceededException;
import com.lms.assessment.model.AssessmentUsageTracking;
import com.lms.assessment.repository.AssessmentUsageTrackingRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Map;

// WHY: Generic usage tracking/enforcement shared across all four assessment features.
// KNOWN LIMITATION: read-then-write, not row-locked — same known concurrency
// limitation as other Usage Services in this codebase; acceptable for this traffic pattern.
@Service
public class AssessmentUsageService {

    private final AssessmentUsageTrackingRepository usageRepo;
    private final AssessmentTierResolver tierResolver;

    public AssessmentUsageService(AssessmentUsageTrackingRepository usageRepo,
                                   AssessmentTierResolver tierResolver) {
        this.usageRepo = usageRepo;
        this.tierResolver = tierResolver;
    }

    public void checkAndIncrement(AssessmentUsageLimits.Action action, String email, String organizationId) {
        String tier = tierResolver.resolveTier(organizationId, email);
        String period = YearMonth.now().toString();

        if (AssessmentUsageLimits.isUnlimited(action, tier)) {
            recordUsageOnly(action, email, period);
            return;
        }

        AssessmentUsageTracking usage = usageRepo
                .findByEmailAndPeriodAndAction(email, period, action.name())
                .orElseGet(() -> {
                    AssessmentUsageTracking u = new AssessmentUsageTracking();
                    u.setEmail(email);
                    u.setPeriod(period);
                    u.setAction(action.name());
                    u.setUsageCount(0);
                    return u;
                });

        int limit = AssessmentUsageLimits.limitFor(action, tier);
        if (usage.getUsageCount() >= limit) {
            throw new AssessmentUsageLimitExceededException(
                    email, action.name(), tier, usage.getUsageCount(), limit, period);
        }

        usage.setUsageCount(usage.getUsageCount() + 1);
        usageRepo.save(usage);
    }

    private void recordUsageOnly(AssessmentUsageLimits.Action action, String email, String period) {
        AssessmentUsageTracking usage = usageRepo
                .findByEmailAndPeriodAndAction(email, period, action.name())
                .orElseGet(() -> {
                    AssessmentUsageTracking u = new AssessmentUsageTracking();
                    u.setEmail(email);
                    u.setPeriod(period);
                    u.setAction(action.name());
                    u.setUsageCount(0);
                    return u;
                });
        usage.setUsageCount(usage.getUsageCount() + 1);
        usageRepo.save(usage);
    }

    public Map<String, Object> getUsageStatus(AssessmentUsageLimits.Action action, String email, String organizationId) {
        String tier = tierResolver.resolveTier(organizationId, email);
        String period = YearMonth.now().toString();
        int used = usageRepo.findByEmailAndPeriodAndAction(email, period, action.name())
                .map(AssessmentUsageTracking::getUsageCount)
                .orElse(0);
        int limit = AssessmentUsageLimits.limitFor(action, tier);
        return Map.of(
                "action", action.name(),
                "tier", tier,
                "used", used,
                "limit", AssessmentUsageLimits.isUnlimited(action, tier) ? "unlimited" : limit,
                "period", period
        );
    }
}