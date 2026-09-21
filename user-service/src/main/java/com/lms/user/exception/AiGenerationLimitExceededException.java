package com.lms.user.exception;

public class AiGenerationLimitExceededException extends RuntimeException {

    private final Long userId;
    private final String plan;
    private final int currentCount;
    private final int maxAllowed;
    private final String period;

    public AiGenerationLimitExceededException(Long userId, String plan, int currentCount,
                                               int maxAllowed, String period) {
        super("AI generation limit reached for this month. Plan: " + plan
                + ", used: " + currentCount + "/" + maxAllowed);
        this.userId = userId;
        this.plan = plan;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.period = period;
    }

    public Long getUserId() {
        return userId;
    }

    public String getPlan() {
        return plan;
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