package com.lms.chat.constants;

public final class NotebookUsageLimits {

    public static final int FREE_MONTHLY_LIMIT = 3;
    public static final int PRO_MONTHLY_LIMIT = 30;
    public static final int PREMIUM_MONTHLY_LIMIT = -1; // unlimited

    private NotebookUsageLimits() {
    }

    public static int limitFor(String tier) {
        if (tier == null) {
            return FREE_MONTHLY_LIMIT;
        }
        return switch (tier) {
            case "premium" -> PREMIUM_MONTHLY_LIMIT;
            case "pro"     -> PRO_MONTHLY_LIMIT;
            default        -> FREE_MONTHLY_LIMIT;
        };
    }

    public static boolean isUnlimited(String tier) {
        return limitFor(tier) == -1;
    }
}