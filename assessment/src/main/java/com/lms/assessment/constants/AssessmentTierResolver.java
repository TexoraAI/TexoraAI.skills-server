package com.lms.assessment.constants;

import com.lms.assessment.model.OrgPlanCache;
import com.lms.assessment.model.UserPlanCache;
import com.lms.assessment.repository.OrgPlanCacheRepository;
import com.lms.assessment.repository.UserPlanCacheRepository;
import org.springframework.stereotype.Service;

// WHY: Single shared tier resolver backing all four assessment features
// (quiz, assignment, coding problem, study plan) — do not create per-feature resolvers.
@Service
public class AssessmentTierResolver {

    private final OrgPlanCacheRepository orgPlanCacheRepo;
    private final UserPlanCacheRepository userPlanCacheRepo;

    public AssessmentTierResolver(OrgPlanCacheRepository orgPlanCacheRepo,
                                   UserPlanCacheRepository userPlanCacheRepo) {
        this.orgPlanCacheRepo = orgPlanCacheRepo;
        this.userPlanCacheRepo = userPlanCacheRepo;
    }

    public static String orgPlanToAssessmentTier(String orgPlan) {
        if (orgPlan == null) {
            return "free";
        }
        return switch (orgPlan.toLowerCase()) {
            case "growth"  -> "premium";
            case "starter" -> "pro";
            default        -> "free";
        };
    }

    public String resolveTier(String organizationId, String email) {
        if (organizationId != null) {
            return orgPlanCacheRepo.findById(organizationId)
                    .map(OrgPlanCache::getPlan)
                    .map(AssessmentTierResolver::orgPlanToAssessmentTier)
                    .orElse("free");
        }
        return userPlanCacheRepo.findById(email)
                .map(UserPlanCache::getPlan)
                .orElse("free");
    }
}