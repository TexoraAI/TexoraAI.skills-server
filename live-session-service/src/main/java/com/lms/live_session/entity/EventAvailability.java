package com.lms.live_session.entity;

public enum EventAvailability {
    BUSY, FREE, TENTATIVE, OUT_OF_OFFICE;

    public String getValue() { return this.name(); }

    public static EventAvailability fromValue(String value) {
        if (value == null) return null;
        for (EventAvailability a : values()) {
            if (a.name().equalsIgnoreCase(value)) return a;
        }
        throw new IllegalArgumentException("Invalid availability value: " + value);
    }
}