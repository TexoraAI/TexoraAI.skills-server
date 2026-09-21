package com.lms.chat.exception;

public class NotebookUsageLimitExceededException extends RuntimeException {

    private final String studentEmail;
    private final String tier;
    private final int currentCount;
    private final int maxAllowed;
    private final String period;

    public NotebookUsageLimitExceededException(String studentEmail, String tier,
                                                int currentCount, int maxAllowed, String period) {
        super(String.format(
                "Notebook usage limit reached for %s (tier=%s): %d/%d used in period %s",
                studentEmail, tier, currentCount, maxAllowed, period));
        this.studentEmail = studentEmail;
        this.tier = tier;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.period = period;
    }

    public String getStudentEmail() { return studentEmail; }
    public String getTier()         { return tier; }
    public int getCurrentCount()    { return currentCount; }
    public int getMaxAllowed()      { return maxAllowed; }
    public String getPeriod()       { return period; }
}