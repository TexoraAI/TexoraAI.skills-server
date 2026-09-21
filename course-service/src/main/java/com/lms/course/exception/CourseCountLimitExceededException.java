package com.lms.course.exception;

public class CourseCountLimitExceededException extends RuntimeException {

    private final int currentCount;
    private final int maxAllowed;
    private final String tier;

    public CourseCountLimitExceededException(int currentCount, int maxAllowed, String tier) {
        super("Course limit exceeded for tier '" + tier + "': " + currentCount + "/" + maxAllowed);
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.tier = tier;
    }

    public int getCurrentCount() { return currentCount; }
    public int getMaxAllowed()   { return maxAllowed; }
    public String getTier()      { return tier; }
}