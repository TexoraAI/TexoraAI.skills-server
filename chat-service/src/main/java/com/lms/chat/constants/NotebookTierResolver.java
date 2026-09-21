package com.lms.chat.constants;

import com.lms.chat.entity.OrgPlanCache;
import com.lms.chat.entity.UserPlanCache;
import com.lms.chat.repository.OrgPlanCacheRepository;
import com.lms.chat.repository.UserPlanCacheRepository;
import org.springframework.stereotype.Service;

@Service
public class NotebookTierResolver {

    private final OrgPlanCacheRepository orgPlanCacheRepo;
    private final UserPlanCacheRepository userPlanCacheRepo;

    public NotebookTierResolver(OrgPlanCacheRepository orgPlanCacheRepo,
                                 UserPlanCacheRepository userPlanCacheRepo) {
        this.orgPlanCacheRepo = orgPlanCacheRepo;
        this.userPlanCacheRepo = userPlanCacheRepo;
    }

    public static String orgPlanToNotebookTier(String orgPlan) {
        if (orgPlan == null) {
            return "free";
        }
        return switch (orgPlan) {
            case "growth"  -> "premium";
            case "starter" -> "pro";
            default        -> "free";
        };
    }

    public String resolveTier(String organizationId, String email) {
        if (organizationId != null) {
            return orgPlanCacheRepo.findById(organizationId)
                    .map(OrgPlanCache::getPlan)
                    .map(NotebookTierResolver::orgPlanToNotebookTier)
                    .orElse("free");
        }

        return userPlanCacheRepo.findById(email)
                .map(UserPlanCache::getPlan)
                .orElse("free");
    }
}