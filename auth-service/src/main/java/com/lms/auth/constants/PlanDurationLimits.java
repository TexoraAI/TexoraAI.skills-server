package com.lms.auth.constants;

public final class PlanDurationLimits {
    public static final int[] VALID_DURATIONS_MONTHS = {1, 6, 12};

    public static double discountMultiplierFor(int months) {
        return switch (months) {
            case 12 -> 0.70;
            case 6 -> 0.85;
            default -> 1.0;
        };
    }

    public static boolean isValidDuration(int months) {
        return months == 1 || months == 6 || months == 12;
    }

    public static int calculatePrice(int monthlyBasePrice, int months) {
        return (int) Math.round(monthlyBasePrice * months * discountMultiplierFor(months));
    }

    private PlanDurationLimits() {}
}