package com.lms.live_session.exception;

public class RecordingDurationLimitExceededException extends RuntimeException {

    private final int actualMinutes;
    private final int maxAllowed;
    private final String tier;

    public RecordingDurationLimitExceededException(int actualMinutes, int maxAllowed, String tier) {
        super("Recording duration limit exceeded (tier=" + tier + "): actualMinutes=" + actualMinutes
                + " maxAllowed=" + maxAllowed);
        this.actualMinutes = actualMinutes;
        this.maxAllowed = maxAllowed;
        this.tier = tier;
    }

    public int getActualMinutes() {
        return actualMinutes;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public String getTier() {
        return tier;
    }
}
