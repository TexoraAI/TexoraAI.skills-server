package com.lms.live_session.exception;

public class EmailDispatchLimitExceededException extends RuntimeException {

    private final String email;
    private final String tier;
    private final int currentCount;
    private final int maxAllowed;
    private final String period;

    public EmailDispatchLimitExceededException(String email, String tier, int currentCount, int maxAllowed, String period) {
        super("Email dispatch limit exceeded for " + email + " (tier=" + tier + "): "
                + currentCount + "/" + maxAllowed + " in period " + period);
        this.email = email;
        this.tier = tier;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.period = period;
    }

    public String getEmail() {
        return email;
    }

    public String getTier() {
        return tier;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public String getPeriod() {
        return period;
    }
}
