
package com.lms.course.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SessionType {

    VIDEO,
    READING,
    QUIZ,
    ASSIGNMENT,
    LIVE;

    @JsonCreator
    public static SessionType fromString(String value) {
        if (value == null) {
            return null;
        }
        return SessionType.valueOf(value.trim().toUpperCase());
    }
}