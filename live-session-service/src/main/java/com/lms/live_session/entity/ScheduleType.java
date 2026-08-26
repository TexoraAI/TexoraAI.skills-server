package com.lms.live_session.entity;

public enum ScheduleType {
    SESSION, CLASS, MEETING, TASK, PERSONAL;

    public static ScheduleType fromValue(String value) {
        if (value == null) return null;
        for (ScheduleType t : values()) {
            if (t.name().equalsIgnoreCase(value)) return t;
        }
        throw new IllegalArgumentException("Invalid schedule type: " + value);
    }
}