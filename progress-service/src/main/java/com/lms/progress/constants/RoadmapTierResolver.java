package com.lms.progress.constants;

import com.lms.progress.model.OrgPlanCache;
import com.lms.progress.model.UserPlanCache;
import com.lms.progress.repository.OrgPlanCacheRepository;
import com.lms.progress.repository.UserPlanCacheRepository;
import org.springframework.stereotype.Service;

@Service
public class RoadmapTierResolver {

    private final OrgPlanCacheRepository orgPlanCacheRepo;
    private final UserPlanCacheRepository userPlanCacheRepo;

    public RoadmapTierResolver(OrgPlanCacheRepository orgPlanCacheRepo,
                                UserPlanCacheRepository userPlanCacheRepo) {
        this.orgPlanCacheRepo = orgPlanCacheRepo;
        this.userPlanCacheRepo = userPlanCacheRepo;
    }

    public static String orgPlanToRoadmapTier(String orgPlan) {
        if (orgPlan == null) return "free";
        return switch (orgPlan.toLowerCase()) {
            case "growth"  -> "premium";
            case "starter" -> "pro";
            default        -> "free";
        };
    }

    public String resolveTier(String organizationId, String email) {
        if (organizationId != null && !organizationId.isBlank()) {
            return orgPlanCacheRepo.findById(organizationId)
                    .map(OrgPlanCache::getPlan)
                    .map(RoadmapTierResolver::orgPlanToRoadmapTier)
                    .orElse("free");
        }
        return userPlanCacheRepo.findById(email)
                .map(UserPlanCache::getPlan)
                .orElse("free");
    }
}