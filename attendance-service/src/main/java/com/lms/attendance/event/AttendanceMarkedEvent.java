package com.lms.attendance.event;

import java.time.LocalDate;

public class AttendanceMarkedEvent {

    private String eventType; // ATTENDANCE_MARKED
    private Long batchId;
    private Long studentUserId;
    private String studentEmail;
    private String status; // PRESENT / ABSENT
    private LocalDate attendanceDate;

    public AttendanceMarkedEvent() {}

    public AttendanceMarkedEvent(
            Long batchId,
            Long studentUserId,
            String studentEmail,
            String status,
            LocalDate attendanceDate
    ) {
        this.eventType = "ATTENDANCE_MARKED";
        this.batchId = batchId;
        this.studentUserId = studentUserId;
        this.studentEmail = studentEmail;
        this.status = status;
        this.attendanceDate = attendanceDate;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getBatchId() {
        return batchId;
    }

    public Long getStudentUserId() {
        return studentUserId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }
}
