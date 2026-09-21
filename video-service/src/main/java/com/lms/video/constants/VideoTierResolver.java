package com.lms.video.constants;

import com.lms.video.model.OrgPlanCache;
import com.lms.video.model.UserPlanCache;
import com.lms.video.repository.OrgPlanCacheRepository;
import com.lms.video.repository.UserPlanCacheRepository;
import org.springframework.stereotype.Service;

// WHY: Resolves the effective video tier ("free"/"pro"/"premium") for a caller,
// using the locally-mirrored plan caches instead of calling out to auth-service
// or user-service on every request.
@Service
public class VideoTierResolver {

    private final OrgPlanCacheRepository orgPlanCacheRepository;
    private final UserPlanCacheRepository userPlanCacheRepository;

    public VideoTierResolver(OrgPlanCacheRepository orgPlanCacheRepository,
                              UserPlanCacheRepository userPlanCacheRepository) {
        this.orgPlanCacheRepository = orgPlanCacheRepository;
        this.userPlanCacheRepository = userPlanCacheRepository;
    }

    // WHY: org-level plan names ("growth"/"starter"/"trial") differ from video
    // tier names — this maps org plan -> video tier. Unrecognized/"trial" -> free.
    public static String orgPlanToVideoTier(String orgPlan) {
        if (orgPlan == null) {
            return "free";
        }
        return switch (orgPlan.toLowerCase()) {
            case "growth" -> "premium";
            case "starter" -> "pro";
            default -> "free"; // "trial" or unrecognized
        };
    }

    // WHY: single entry point callers use to get a caller's video tier — org
    // membership takes precedence over the standalone user plan.
    public String resolveTier(String organizationId, String email) {
        if (organizationId != null) {
            return orgPlanCacheRepository.findById(organizationId)
                    .map(OrgPlanCache::getPlan)
                    .map(VideoTierResolver::orgPlanToVideoTier)
                    .orElse("free");
        }

        return userPlanCacheRepository.findById(email)
                .map(UserPlanCache::getPlan)
                .orElse("free");
    }
}