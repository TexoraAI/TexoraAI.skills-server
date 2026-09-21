package com.lms.auth.exception;

public class SeatLimitExceededException extends RuntimeException {

    private final String orgId;
    private final String limitType;
    private final int currentCount;
    private final int maxAllowed;

    public SeatLimitExceededException(String orgId, String limitType, int currentCount, int maxAllowed) {
        super("Seat limit reached for organization " + orgId + ": " + limitType);
        this.orgId = orgId;
        this.limitType = limitType;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
    }

    public String getOrgId() { return orgId; }
    public String getLimitType() { return limitType; }
    public int getCurrentCount() { return currentCount; }
    public int getMaxAllowed() { return maxAllowed; }
}