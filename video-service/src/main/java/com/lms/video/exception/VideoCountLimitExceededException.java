package com.lms.video.exception;

// WHY: Thrown when the caller has already reached the max number of videos allowed for their tier.
public class VideoCountLimitExceededException extends RuntimeException {

    private final int currentCount;
    private final int maxAllowed;
    private final String tier;

    public VideoCountLimitExceededException(int currentCount, int maxAllowed, String tier) {
        super("Video count " + currentCount + " has reached max allowed " + maxAllowed
                + " for tier '" + tier + "'");
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.tier = tier;
    }

    public int getCurrentCount() { return currentCount; }
    public int getMaxAllowed()   { return maxAllowed; }
    public String getTier()      { return tier; }
}