package com.lms.user.constants;

import com.lms.user.model.User;
import java.util.List;

public final class ResumeTierResolver {

    // Ordered rank, lowest to highest
    private static final List<String> TIER_RANK = List.of("free", "pro", "premium");

    public static String orgPlanToResumeTier(String orgPlan) {
        if (orgPlan == null) return "free";
        return switch (orgPlan.toLowerCase()) {
            case "growth" -> "premium";
            case "starter" -> "pro";
            default -> "free"; // "trial" or unrecognized
        };
    }

    public static String resolveEffectiveTier(User user) {
        String individualTier;
        if (user.getOrganizationId() != null) {
            // org-bound: base tier from org plan, or their resume-specific override, whichever is higher
            String orgDerivedTier = orgPlanToResumeTier(user.getOrgPlan());
            String overrideTier = user.getResumePlanOverride() != null
                ? user.getResumePlanOverride().toLowerCase() : "free";
            individualTier = higherOf(orgDerivedTier, overrideTier);
        } else {
            // standalone: use the existing general "plan" field directly
            individualTier = user.getPlan() != null ? user.getPlan().toLowerCase() : "free";
        }
        return individualTier;
    }

    private static String higherOf(String a, String b) {
        int rankA = TIER_RANK.indexOf(a);
        int rankB = TIER_RANK.indexOf(b);
        return (Math.max(rankA, rankB) == rankA) ? a : b;
    }

    private ResumeTierResolver() {}
}