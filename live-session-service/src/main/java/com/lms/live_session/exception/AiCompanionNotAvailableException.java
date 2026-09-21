package com.lms.live_session.exception;

public class AiCompanionNotAvailableException extends RuntimeException {

    private final String email;
    private final String tier;

    public AiCompanionNotAvailableException(String email, String tier, String message) {
        super(message);
        this.email = email;
        this.tier = tier;
    }

    public String getEmail() {
        return email;
    }

    public String getTier() {
        return tier;
    }
}
