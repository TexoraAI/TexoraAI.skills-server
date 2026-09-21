package com.lms.live_session.service;

import com.lms.live_session.entity.UserPlanCache;
import com.lms.live_session.repository.OrgPlanCacheRepository;
import com.lms.live_session.repository.UserPlanCacheRepository;
import org.springframework.stereotype.Service;

/**
 * Resolves the effective plan "tier" (free | pro | premium) for a live-session
 * operation, preferring org-level plan when an organizationId is present and
 * falling back to individual user plan otherwise.
 *
 * ASSUMPTION FLAGGED: resolveTier(Long organizationId, ...) stringifies the
 * Long organizationId and looks it up against OrgPlanCache, which is keyed by
 * the String organizationId (UUID text) coming from auth-events. This assumes
 * this service's Long organizationId, stringified, matches auth-service's
 * Organization UUID.toString() exactly. If this service's Long organizationId
 * is actually a different ID space than auth-service's UUID, this resolver
 * will silently always miss the cache and return "free" for every org user.
 *
 * Before relying on this in production, verify by tracing what value
 * LiveSessionAccessValidator.validateAndResolveOrganizationId actually parses
 * organizationId FROM — i.e. what jwtUtil.extractOrganizationId returns and
 * how that becomes a Long — and confirm it traces back to the same UUID
 * auth-service issues (and that the Long conversion round-trips losslessly
 * back to that UUID string).
 */
@Service
public class LiveSessionTierResolver {

    private final OrgPlanCacheRepository orgPlanCacheRepository;
    private final UserPlanCacheRepository userPlanCacheRepository;

    public LiveSessionTierResolver(OrgPlanCacheRepository orgPlanCacheRepository,
                                    UserPlanCacheRepository userPlanCacheRepository) {
        this.orgPlanCacheRepository = orgPlanCacheRepository;
        this.userPlanCacheRepository = userPlanCacheRepository;
    }

    public static String orgPlanToTier(String orgPlan) {
        if (orgPlan == null) {
            return "free";
        }
        return switch (orgPlan.toLowerCase()) {
            case "growth" -> "premium";
            case "starter" -> "pro";
            default -> "free";
        };
    }

//    public String resolveTier(Long organizationId, String email) {
//        if (organizationId != null) {
//            return orgPlanCacheRepository.findById(organizationId.toString())
//                    .map(c -> orgPlanToTier(c.getPlan()))
//                    .orElse("free");
//        }
//        return userPlanCacheRepository.findById(email)
//                .map(UserPlanCache::getPlan)
//                .orElse("free");
//    }
    public String resolveTier(Long organizationId, String email) {
        if (organizationId != null) {
            return orgPlanCacheRepository.findById(organizationId.toString())
                    .map(c -> orgPlanToTier(c.getPlan()))
                    .orElse("free");
        }
        return userPlanCacheRepository.findById(email)
                .map(UserPlanCache::getPlan)
                .orElse("free");
    }

    // NEW — org id as it actually comes off the JWT: a UUID string, matching
    // OrgPlanCache's real key type. No Long parsing, no Long.toString()
    // round-trip — the raw claim value is used as-is against the cache.
    public String resolveTier(String organizationId, String email) {
        if (organizationId != null && !organizationId.isBlank()) {
            return orgPlanCacheRepository.findById(organizationId)
                    .map(c -> orgPlanToTier(c.getPlan()))
                    .orElse("free");
        }
        return userPlanCacheRepository.findById(email)
                .map(UserPlanCache::getPlan)
                .orElse("free");
    }
}
