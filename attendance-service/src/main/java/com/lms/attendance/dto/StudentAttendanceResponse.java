package com.lms.attendance.dto;

import java.time.LocalDate;

public class StudentAttendanceResponse {

    private LocalDate attendanceDate;
    private String status;

    public StudentAttendanceResponse(LocalDate attendanceDate, String status) {
        this.attendanceDate = attendanceDate;
        this.status = status;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public String getStatus() {
        return status;
    }
}
