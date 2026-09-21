package com.lms.file.exception;

/**
 * Thrown when an uploaded file exceeds the max single-file size allowed for
 * the caller's current tier.
 */
public class FileSizeLimitExceededException extends RuntimeException {

    private final long actualSize;
    private final long maxAllowed;
    private final String tier;

    public FileSizeLimitExceededException(long actualSize, long maxAllowed, String tier) {
        super("File size " + actualSize + " bytes exceeds the maximum of " + maxAllowed
                + " bytes allowed for tier '" + tier + "'");
        this.actualSize = actualSize;
        this.maxAllowed = maxAllowed;
        this.tier = tier;
    }

    public long getActualSize() {
        return actualSize;
    }

    public long getMaxAllowed() {
        return maxAllowed;
    }

    public String getTier() {
        return tier;
    }
}
