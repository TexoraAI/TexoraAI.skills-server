//package com.lms.attendance.event;
//
//import java.time.LocalDate;
//
//public class AttendanceMarkedEvent {
//
//    private String eventType; // ATTENDANCE_MARKED
//    private Long batchId;
//    private Long studentUserId;
//    private String studentEmail;
//    private String status; // PRESENT / ABSENT
//    private LocalDate attendanceDate;
//
//    public AttendanceMarkedEvent() {}
//
//    public AttendanceMarkedEvent(
//            Long batchId,
//            Long studentUserId,
//            String studentEmail,
//            String status,
//            LocalDate attendanceDate
//    ) {
//        this.eventType = "ATTENDANCE_MARKED";
//        this.batchId = batchId;
//        this.studentUserId = studentUserId;
//        this.studentEmail = studentEmail;
//        this.status = status;
//        this.attendanceDate = attendanceDate;
//    }
//
//    public String getEventType() {
//        return eventType;
//    }
//
//    public Long getBatchId() {
//        return batchId;
//    }
//
//    public Long getStudentUserId() {
//        return studentUserId;
//    }
//
//    public String getStudentEmail() {
//        return studentEmail;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public LocalDate getAttendanceDate() {
//        return attendanceDate;
//    }
//}
package com.lms.attendance.event;

import java.time.LocalDate;

public class AttendanceMarkedEvent {

    private Long batchId;
    private Long studentUserId;
    private String studentEmail;
    private String status;
    private LocalDate date;
    private String trainerEmail; // ✅ ADDED — needed for trainer notification

    public AttendanceMarkedEvent() {}

    public AttendanceMarkedEvent(Long batchId, Long studentUserId,
                                  String studentEmail, String status,
                                  LocalDate date) {
        this.batchId = batchId;
        this.studentUserId = studentUserId;
        this.studentEmail = studentEmail;
        this.status = status;
        this.date = date;
    }

    // ✅ ADDED constructor with trainerEmail
    public AttendanceMarkedEvent(Long batchId, Long studentUserId,
                                  String studentEmail, String status,
                                  LocalDate date, String trainerEmail) {
        this.batchId = batchId;
        this.studentUserId = studentUserId;
        this.studentEmail = studentEmail;
        this.status = status;
        this.date = date;
        this.trainerEmail = trainerEmail;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getStudentUserId() { return studentUserId; }
    public void setStudentUserId(Long v) { this.studentUserId = v; }
    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String v) { this.studentEmail = v; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getTrainerEmail() { return trainerEmail; } // ✅ ADDED
    public void setTrainerEmail(String v) { this.trainerEmail = v; } // ✅ ADDED
}