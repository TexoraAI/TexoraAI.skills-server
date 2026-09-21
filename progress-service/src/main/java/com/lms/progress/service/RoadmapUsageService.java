package com.lms.progress.service;

import com.lms.progress.constants.RoadmapTierResolver;
import com.lms.progress.constants.RoadmapUsageLimits;
import com.lms.progress.exception.RoadmapUsageLimitExceededException;
import com.lms.progress.model.RoadmapUsageTracking;
import com.lms.progress.repository.RoadmapUsageTrackingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.Map;

@Service
public class RoadmapUsageService {

    private final RoadmapUsageTrackingRepository usageRepo;
    private final RoadmapTierResolver roadmapTierResolver;

    public RoadmapUsageService(RoadmapUsageTrackingRepository usageRepo,
                                RoadmapTierResolver roadmapTierResolver) {
        this.usageRepo = usageRepo;
        this.roadmapTierResolver = roadmapTierResolver;
    }

    @Transactional
    public void checkAndIncrement(Long ownerId, String email, String organizationId) {
        String tier = roadmapTierResolver.resolveTier(organizationId, email);
        String period = YearMonth.now().toString();

        if (RoadmapUsageLimits.isUnlimited(tier)) {
            recordUsageOnly(ownerId, period);
            return;
        }

        RoadmapUsageTracking usage = usageRepo.findByOwnerIdAndPeriod(ownerId, period)
                .orElseGet(() -> {
                    RoadmapUsageTracking u = new RoadmapUsageTracking();
                    u.setOwnerId(ownerId);
                    u.setPeriod(period);
                    u.setRoadmapCount(0);
                    return u;
                });

        int limit = RoadmapUsageLimits.limitFor(tier);
        if (usage.getRoadmapCount() >= limit) {
            throw new RoadmapUsageLimitExceededException(ownerId, tier, usage.getRoadmapCount(), limit, period);
        }

        usage.setRoadmapCount(usage.getRoadmapCount() + 1);
        usageRepo.save(usage);
    }

    private void recordUsageOnly(Long ownerId, String period) {
        RoadmapUsageTracking usage = usageRepo.findByOwnerIdAndPeriod(ownerId, period)
                .orElseGet(() -> {
                    RoadmapUsageTracking u = new RoadmapUsageTracking();
                    u.setOwnerId(ownerId);
                    u.setPeriod(period);
                    u.setRoadmapCount(0);
                    return u;
                });
        usage.setRoadmapCount(usage.getRoadmapCount() + 1);
        usageRepo.save(usage);
    }

    public Map<String, Object> getUsageStatus(Long ownerId, String email, String organizationId) {
        String tier = roadmapTierResolver.resolveTier(organizationId, email);
        String period = YearMonth.now().toString();
        int used = usageRepo.findByOwnerIdAndPeriod(ownerId, period)
                .map(RoadmapUsageTracking::getRoadmapCount)
                .orElse(0);
        int limit = RoadmapUsageLimits.limitFor(tier);
        return Map.of(
            "tier", tier,
            "used", used,
            "limit", RoadmapUsageLimits.isUnlimited(tier) ? "unlimited" : limit,
            "period", period
        );
    }
}