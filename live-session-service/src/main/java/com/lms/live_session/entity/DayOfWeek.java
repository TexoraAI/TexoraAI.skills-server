package com.lms.live_session.entity;

/**
 * Represents the day of the week for an availability slot.
 * NOTE: This is a custom enum distinct from java.time.DayOfWeek.
 * When converting from java.time.DayOfWeek, always fully qualify
 * (java.time.DayOfWeek) or import with an alias to avoid ambiguity.
 */
public enum DayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    /**
     * Converts java.time.DayOfWeek to this enum.
     */
    public static DayOfWeek fromJavaTime(java.time.DayOfWeek javaDayOfWeek) {
        return DayOfWeek.valueOf(javaDayOfWeek.name());
    }

    /**
     * Parses common short/long forms (MON, Monday, MONDAY, etc.) into this enum.
     */
    public static DayOfWeek fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("dayOfWeek must not be null");
        }
        String normalized = value.trim().toUpperCase();
        switch (normalized) {
            case "MON":
            case "MONDAY":
                return MONDAY;
            case "TUE":
            case "TUESDAY":
                return TUESDAY;
            case "WED":
            case "WEDNESDAY":
                return WEDNESDAY;
            case "THU":
            case "THURSDAY":
                return THURSDAY;
            case "FRI":
            case "FRIDAY":
                return FRIDAY;
            case "SAT":
            case "SATURDAY":
                return SATURDAY;
            case "SUN":
            case "SUNDAY":
                return SUNDAY;
            default:
                throw new IllegalArgumentException("Invalid dayOfWeek: " + value);
        }
    }

    /**
     * Sort order used for MON-SUN ordering (0 = Monday ... 6 = Sunday).
     */
    public int sortOrder() {
        return this.ordinal();
    }
}