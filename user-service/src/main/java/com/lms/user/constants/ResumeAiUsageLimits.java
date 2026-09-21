package com.lms.user.constants;

public final class ResumeAiUsageLimits {

    public static final int FREE_MONTHLY_LIMIT = 3;
    public static final int PRO_MONTHLY_LIMIT = 15;
    // premium = unlimited, represented as -1 (no numeric cap enforced)
    public static final int PREMIUM_MONTHLY_LIMIT = -1;

    public static int limitFor(String plan) {
        String p = plan == null ? "free" : plan.toLowerCase();
        return switch (p) {
            case "premium" -> PREMIUM_MONTHLY_LIMIT;
            case "pro" -> PRO_MONTHLY_LIMIT;
            default -> FREE_MONTHLY_LIMIT;
        };
    }

    public static boolean isUnlimited(String plan) {
        return limitFor(plan) == PREMIUM_MONTHLY_LIMIT;
    }

    private ResumeAiUsageLimits() {}
}