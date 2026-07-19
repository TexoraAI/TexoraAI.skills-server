package com.lms.attendance.dto;

import java.time.LocalDate;

public class AttendanceRecordResponse {

    private Long batchId;
    private Long studentUserId;
    private String studentEmail;
    private String trainerEmail;
    private LocalDate attendanceDate;
    private String status;
    private String organizationId;

    public AttendanceRecordResponse() {}

    public AttendanceRecordResponse(Long batchId, Long studentUserId, String studentEmail,
                                     String trainerEmail, LocalDate attendanceDate,
                                     String status, String organizationId) {
        this.batchId = batchId;
        this.studentUserId = studentUserId;
        this.studentEmail = studentEmail;
        this.trainerEmail = trainerEmail;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.organizationId = organizationId;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public Long getStudentUserId() { return studentUserId; }
    public void setStudentUserId(Long studentUserId) { this.studentUserId = studentUserId; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}