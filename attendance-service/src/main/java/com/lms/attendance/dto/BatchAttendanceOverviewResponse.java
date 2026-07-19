package com.lms.attendance.dto;

public class BatchAttendanceOverviewResponse {

    private Long batchId;
    private String trainerEmail;
    private int studentCount;
    private int sessionsMarked;

    public BatchAttendanceOverviewResponse() {}

    public BatchAttendanceOverviewResponse(Long batchId, String trainerEmail, int studentCount, int sessionsMarked) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
        this.studentCount = studentCount;
        this.sessionsMarked = sessionsMarked;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    public int getSessionsMarked() { return sessionsMarked; }
    public void setSessionsMarked(int sessionsMarked) { this.sessionsMarked = sessionsMarked; }
}