package com.lms.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public class MarkAttendanceRequest {

    private Long batchId;
    private LocalDate attendanceDate;
    private List<StudentAttendance> attendances;

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public List<StudentAttendance> getAttendances() {
        return attendances;
    }

    public void setAttendances(List<StudentAttendance> attendances) {
        this.attendances = attendances;
    }

    // =====================
    // INNER CLASS (UPDATED)
    // =====================
    public static class StudentAttendance {

        private Long studentUserId;
        private String studentEmail; // ✅ REQUIRED (DB NOT NULL)
        private String status; // PRESENT / ABSENT / LATE

        public Long getStudentUserId() {
            return studentUserId;
        }

        public void setStudentUserId(Long studentUserId) {
            this.studentUserId = studentUserId;
        }

        public String getStudentEmail() {
            return studentEmail;
        }

        public void setStudentEmail(String studentEmail) {
            this.studentEmail = studentEmail;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
