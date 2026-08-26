package com.lms.live_session.entity;

public enum EventAttendeeType {
    REQUIRED, OPTIONAL;

    public static EventAttendeeType fromValue(String value) {
        if (value == null) return null;
        for (EventAttendeeType t : values()) {
            if (t.name().equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Invalid attendee type: " + value);
    }
}