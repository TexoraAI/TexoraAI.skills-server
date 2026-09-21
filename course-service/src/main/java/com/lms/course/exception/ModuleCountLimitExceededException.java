package com.lms.course.exception;

public class ModuleCountLimitExceededException extends RuntimeException {

    private final Long courseId;
    private final int currentCount;
    private final int maxAllowed;
    private final String tier;

    public ModuleCountLimitExceededException(Long courseId, int currentCount, int maxAllowed, String tier) {
        super("Module limit exceeded for course " + courseId + " on tier '" + tier + "': "
                + currentCount + "/" + maxAllowed);
        this.courseId = courseId;
        this.currentCount = currentCount;
        this.maxAllowed = maxAllowed;
        this.tier = tier;
    }

    public Long getCourseId()    { return courseId; }
    public int getCurrentCount() { return currentCount; }
    public int getMaxAllowed()   { return maxAllowed; }
    public String getTier()      { return tier; }
}