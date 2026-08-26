package com.lms.live_session.entity;

public enum EventAttendeeStatus {
    PENDING, ACCEPTED, DECLINED, TENTATIVE;

    public static EventAttendeeStatus fromValue(String value) {
        if (value == null) return null;
        for (EventAttendeeStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) return s;
        }
        throw new IllegalArgumentException("Invalid attendee status: " + value);
    }
}