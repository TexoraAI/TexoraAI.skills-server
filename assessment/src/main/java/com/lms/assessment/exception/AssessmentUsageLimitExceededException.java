package com.lms.assessment.exception;

public class AssessmentUsageLimitExceededException extends RuntimeException {

    private final String email;
    private final String action;
    private final String tier;
    private final int currentCount;
    private final int maxAllowed;
    private final String period;

    public AssessmentUsageLimitExceededException(String email, String action, String tier,
                                                  int currentCount, int maxAllowed, String period) {
        super(String.format(
                "Usage limit exceeded for action=%s, tier=%s, email=%s: %d/%d used in period=%s",
                action, tier, email, currentCount, maxAllowed, period));
        this.email = email;
        this.action = action;
        this.tier = tier;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.period = period;
    }

    public String getEmail()        { return email; }
    public String getAction()       { return action; }
    public String getTier()         { return tier; }
    public int getCurrentCount()    { return currentCount; }
    public int getMaxAllowed()      { return maxAllowed; }
    public String getPeriod()       { return period; }
}