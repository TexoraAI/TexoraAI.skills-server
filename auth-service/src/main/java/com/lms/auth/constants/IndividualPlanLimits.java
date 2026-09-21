package com.lms.auth.constants;

public final class IndividualPlanLimits {
    public static final int FREE_PRICE = 0;
    public static final int PRO_STUDENT_PRICE = 100;       // existing, unchanged
    public static final int PRO_TRAINER_PRICE = 100;       // existing, unchanged
    public static final int PREMIUM_STUDENT_PRICE = 39900;   // placeholder, adjust
    public static final int PREMIUM_TRAINER_PRICE = 99900;   // placeholder, adjust

    public static int resolvePrice(String planName, String role) {
        if ("free".equalsIgnoreCase(planName)) return FREE_PRICE;
        if ("pro".equalsIgnoreCase(planName)) {
            if ("TRAINER".equalsIgnoreCase(role)) return PRO_TRAINER_PRICE;
            if ("STUDENT".equalsIgnoreCase(role)) return PRO_STUDENT_PRICE;
            throw new IllegalArgumentException("Pro plan not available for role: " + role);
        }
        if ("premium".equalsIgnoreCase(planName)) {
            if ("TRAINER".equalsIgnoreCase(role)) return PREMIUM_TRAINER_PRICE;
            if ("STUDENT".equalsIgnoreCase(role)) return PREMIUM_STUDENT_PRICE;
            throw new IllegalArgumentException("Premium plan not available for role: " + role);
        }
        throw new IllegalArgumentException("Unknown plan: " + planName);
    }

    public static boolean isValidPlanName(String planName) {
        return "free".equalsIgnoreCase(planName)
                || "pro".equalsIgnoreCase(planName)
                || "premium".equalsIgnoreCase(planName);
    }

    private IndividualPlanLimits() {}
}