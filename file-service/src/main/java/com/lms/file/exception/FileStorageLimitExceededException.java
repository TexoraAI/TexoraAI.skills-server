package com.lms.file.exception;

/**
 * Thrown when adding a new file would push the caller's total storage usage
 * beyond the capacity allowed for their current tier.
 */
public class FileStorageLimitExceededException extends RuntimeException {

    private final long currentUsage;
    private final long attemptedAdd;
    private final long capacity;
    private final String tier;

    public FileStorageLimitExceededException(long currentUsage, long attemptedAdd, long capacity, String tier) {
        super("Adding " + attemptedAdd + " bytes to current usage of " + currentUsage
                + " bytes would exceed the storage capacity of " + capacity
                + " bytes allowed for tier '" + tier + "'");
        this.currentUsage = currentUsage;
        this.attemptedAdd = attemptedAdd;
        this.capacity = capacity;
        this.tier = tier;
    }

    public long getCurrentUsage() {
        return currentUsage;
    }

    public long getAttemptedAdd() {
        return attemptedAdd;
    }

    public long getCapacity() {
        return capacity;
    }

    public String getTier() {
        return tier;
    }
}
