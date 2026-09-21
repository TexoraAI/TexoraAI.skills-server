package com.lms.file.constants;

import com.lms.file.model.UserPlanCache;
import com.lms.file.repository.OrgPlanCacheRepository;
import com.lms.file.repository.UserPlanCacheRepository;
import org.springframework.stereotype.Service;

/**
 * Resolves the effective file-service tier ("free" | "pro" | "premium") for
 * a given organization or user, based on the locally cached auth-service
 * plan data (org_plan_cache / user_plan_cache).
 */
@Service
public class FileTierResolver {

    private final OrgPlanCacheRepository orgPlanCacheRepository;
    private final UserPlanCacheRepository userPlanCacheRepository;

    public FileTierResolver(OrgPlanCacheRepository orgPlanCacheRepository,
                             UserPlanCacheRepository userPlanCacheRepository) {
        this.orgPlanCacheRepository = orgPlanCacheRepository;
        this.userPlanCacheRepository = userPlanCacheRepository;
    }

    public static String orgPlanToFileTier(String orgPlan) {
        if (orgPlan == null) {
            return "free";
        }
        return switch (orgPlan.toLowerCase()) {
            case "growth" -> "premium";
            case "starter" -> "pro";
            default -> "free";
        };
    }

    public String resolveTier(String organizationId, String email) {
        if (organizationId != null) {
            return orgPlanCacheRepository.findById(organizationId)
                    .map(c -> orgPlanToFileTier(c.getPlan()))
                    .orElse("free");
        }
        return userPlanCacheRepository.findById(email)
                .map(UserPlanCache::getPlan)
                .orElse("free");
    }
}
