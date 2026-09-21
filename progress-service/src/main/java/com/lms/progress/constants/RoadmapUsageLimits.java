package com.lms.progress.constants;

public class RoadmapUsageLimits {

    public static final int FREE_MONTHLY_LIMIT = 3;
    public static final int PRO_MONTHLY_LIMIT = 8;
    public static final int PREMIUM_MONTHLY_LIMIT = -1; // unlimited

    private RoadmapUsageLimits() {}

    public static int limitFor(String tier) {
        if (tier == null) return FREE_MONTHLY_LIMIT;
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_MONTHLY_LIMIT;
            case "pro"     -> PRO_MONTHLY_LIMIT;
            default        -> FREE_MONTHLY_LIMIT;
        };
    }

    public static boolean isUnlimited(String tier) {
        return limitFor(tier) == -1;
    }
}