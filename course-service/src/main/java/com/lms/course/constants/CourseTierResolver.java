package com.lms.course.constants;

import com.lms.course.model.OrgPlanCache;
import com.lms.course.model.UserPlanCache;
import com.lms.course.repository.OrgPlanCacheRepository;
import com.lms.course.repository.UserPlanCacheRepository;
import org.springframework.stereotype.Service;

@Service
public class CourseTierResolver {

    private final OrgPlanCacheRepository orgPlanCacheRepo;
    private final UserPlanCacheRepository userPlanCacheRepo;

    public CourseTierResolver(OrgPlanCacheRepository orgPlanCacheRepo,
                               UserPlanCacheRepository userPlanCacheRepo) {
        this.orgPlanCacheRepo = orgPlanCacheRepo;
        this.userPlanCacheRepo = userPlanCacheRepo;
    }

    public static String orgPlanToCourseTier(String orgPlan) {
        if (orgPlan == null) return "free";
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
                    .map(CourseTierResolver::orgPlanToCourseTier)
                    .orElse("free");
        }
        return userPlanCacheRepo.findById(email)
                .map(UserPlanCache::getPlan)
                .orElse("free");
    }
}