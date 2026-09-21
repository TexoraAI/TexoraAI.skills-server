package com.lms.video.exception;

// WHY: Thrown when adding a new video would push total storage usage past the caller's tier capacity.
public class VideoStorageLimitExceededException extends RuntimeException {

    private final long currentUsage;
    private final long attemptedAdd;
    private final long capacity;
    private final String tier;

    public VideoStorageLimitExceededException(long currentUsage, long attemptedAdd, long capacity, String tier) {
        super("Storage usage " + currentUsage + " bytes + attempted " + attemptedAdd
                + " bytes exceeds capacity " + capacity + " bytes for tier '" + tier + "'");
        this.currentUsage = currentUsage;
        this.attemptedAdd = attemptedAdd;
        this.capacity = capacity;
        this.tier = tier;
    }

    public long getCurrentUsage()  { return currentUsage; }
    public long getAttemptedAdd()  { return attemptedAdd; }
    public long getCapacity()      { return capacity; }
    public String getTier()        { return tier; }
}