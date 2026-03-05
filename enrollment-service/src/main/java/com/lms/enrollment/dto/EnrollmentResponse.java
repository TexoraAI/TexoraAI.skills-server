package com.lms.enrollment.dto;

import java.time.Instant;

public class EnrollmentResponse {

    private Long enrollmentId;
    private Long userId;
    private Long courseId;
    private Instant enrolledAt;

    public EnrollmentResponse() {}
//vfvfvfv
    public Long getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(Long enrollmentId) { this.enrollmentId = enrollmentId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Instant getEnrolledAt() { return enrolledAt; }
    public void setEnrolledAt(Instant enrolledAt) { this.enrolledAt = enrolledAt; }
}
