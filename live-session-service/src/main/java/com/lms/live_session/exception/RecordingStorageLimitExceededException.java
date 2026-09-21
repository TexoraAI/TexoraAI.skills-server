package com.lms.live_session.exception;

public class RecordingStorageLimitExceededException extends RuntimeException {

    private final long currentUsage;
    private final long attemptedAdd;
    private final long capacity;
    private final String tier;

    public RecordingStorageLimitExceededException(long currentUsage, long attemptedAdd, long capacity, String tier) {
        super("Recording storage limit exceeded (tier=" + tier + "): currentUsage=" + currentUsage
                + " attemptedAdd=" + attemptedAdd + " capacity=" + capacity);
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
