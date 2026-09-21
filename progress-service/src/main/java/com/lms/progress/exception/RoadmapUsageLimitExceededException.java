package com.lms.progress.exception;

public class RoadmapUsageLimitExceededException extends RuntimeException {

    private final Long ownerId;
    private final String tier;
    private final int currentCount;
    private final int maxAllowed;
    private final String period;

    public RoadmapUsageLimitExceededException(Long ownerId, String tier, int currentCount,
                                                int maxAllowed, String period) {
        super(String.format(
                "Roadmap usage limit exceeded for ownerId=%d tier=%s period=%s (%d/%d used)",
                ownerId, tier, period, currentCount, maxAllowed));
        this.ownerId = ownerId;
        this.tier = tier;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.period = period;
    }

    public Long getOwnerId()      { return ownerId; }
    public String getTier()       { return tier; }
    public int getCurrentCount()  { return currentCount; }
    public int getMaxAllowed()    { return maxAllowed; }
    public String getPeriod()     { return period; }
}