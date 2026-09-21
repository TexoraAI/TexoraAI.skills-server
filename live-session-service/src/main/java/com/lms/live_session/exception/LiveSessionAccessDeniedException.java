package com.lms.live_session.exception;

public class LiveSessionAccessDeniedException extends RuntimeException {
    public LiveSessionAccessDeniedException(String message) {
        super(message);
    }
}