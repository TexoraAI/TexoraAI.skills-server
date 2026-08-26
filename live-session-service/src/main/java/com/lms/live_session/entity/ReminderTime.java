package com.lms.live_session.entity;

/**
 * Represents how far in advance a reminder should fire, relative to the
 * scheduled event/schedule time.
 *
 * Note: raw enum constant names cannot start with a digit in Java (e.g. "5MIN"
 * is not a legal identifier), so constants are named MIN_5, MIN_10, etc.
 * The wire-format value (what clients send/receive, e.g. "5MIN") is carried
 * in the `value` field and used by JPA via @Enumerated(EnumType.STRING) on
 * Reminder.reminderTime combined with fromValue()/getValue() below.
 */
public enum ReminderTime {

    NO_REMINDER("NO_REMINDER", 0),
    MIN_5("5MIN", 5),
    MIN_10("10MIN", 10),
    MIN_30("30MIN", 30),
    HOUR_1("1HOUR", 60),
    DAY_1("1DAY", 1440);

    private final String value;
    private final long minutesBefore;

    ReminderTime(String value, long minutesBefore) {
        this.value = value;
        this.minutesBefore = minutesBefore;
    }

    public String getValue() {
        return value;
    }

    public long getMinutesBefore() {
        return minutesBefore;
    }

    public static ReminderTime fromValue(String value) {
        if (value == null) {
            return NO_REMINDER;
        }
        for (ReminderTime rt : values()) {
            if (rt.value.equalsIgnoreCase(value) || rt.name().equalsIgnoreCase(value)) {
                return rt;
            }
        }
        throw new IllegalArgumentException("Unknown reminder time: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}