package com.lms.file.exception;

/**
 * Thrown when uploading a new file would exceed the max number of files
 * allowed for the caller's current tier.
 */
public class FileCountLimitExceededException extends RuntimeException {

    private final int currentCount;
    private final int maxAllowed;
    private final String tier;

    public FileCountLimitExceededException(int currentCount, int maxAllowed, String tier) {
        super("File count " + currentCount + " has reached the maximum of " + maxAllowed
                + " allowed for tier '" + tier + "'");
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.tier = tier;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public String getTier() {
        return tier;
    }
}
