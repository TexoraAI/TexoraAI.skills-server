package com.lms.live_session.entity;

public enum ScheduleReminder {
    NO_REMINDER("NO_REMINDER"),
    MIN_5("5MIN"),
    MIN_10("10MIN"),
    MIN_30("30MIN"),
    HOUR_1("1HOUR"),
    DAY_1("1DAY");

    private final String value;

    ScheduleReminder(String value) { this.value = value; }

    public String getValue() { return value; }

    public static ScheduleReminder fromValue(String value) {
        if (value == null) return null;
        for (ScheduleReminder r : values()) {
            if (r.value.equalsIgnoreCase(value)) return r;
        }
        throw new IllegalArgumentException("Invalid reminder value: " + value);
    }

    @Override
    public String toString() { return value; }
}