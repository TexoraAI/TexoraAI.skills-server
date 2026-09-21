package com.lms.video.exception;

// WHY: Thrown when a single uploaded video exceeds the per-video size cap for the caller's tier.
public class VideoSizeLimitExceededException extends RuntimeException {

    private final long actualSize;
    private final long maxAllowed;
    private final String tier;

    public VideoSizeLimitExceededException(long actualSize, long maxAllowed, String tier) {
        super("Video size " + actualSize + " bytes exceeds max allowed " + maxAllowed
                + " bytes for tier '" + tier + "'");
        this.actualSize = actualSize;
        this.maxAllowed = maxAllowed;
        this.tier = tier;
    }

    public long getActualSize() { return actualSize; }
    public long getMaxAllowed() { return maxAllowed; }
    public String getTier()     { return tier; }
}