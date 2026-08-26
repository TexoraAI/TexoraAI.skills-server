package com.lms.live_session.entity;

public enum EventRepeat {
    DOES_NOT_REPEAT, DAILY, WEEKLY, MONTHLY, CUSTOM;

    public String getValue() { return this.name(); }

    public static EventRepeat fromValue(String value) {
        if (value == null) return null;
        for (EventRepeat r : values()) {
            if (r.name().equalsIgnoreCase(value)) return r;
        }
        throw new IllegalArgumentException("Invalid repeat value: " + value);
    }
}