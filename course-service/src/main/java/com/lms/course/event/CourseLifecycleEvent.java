package com.lms.course.event;

public class CourseLifecycleEvent {

    private String type;
    private Long courseId;

    public CourseLifecycleEvent() {}

    public CourseLifecycleEvent(String type, Long courseId) {
        this.type = type;
        this.courseId = courseId;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
}