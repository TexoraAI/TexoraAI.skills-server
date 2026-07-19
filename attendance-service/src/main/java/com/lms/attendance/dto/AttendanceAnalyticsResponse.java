package com.lms.attendance.dto;

public class AttendanceAnalyticsResponse {

    private int totalSessions;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private double attendancePercentage;

    public AttendanceAnalyticsResponse() {}

    public AttendanceAnalyticsResponse(int totalSessions, int presentCount, int absentCount,
                                        int lateCount, double attendancePercentage) {
        this.totalSessions = totalSessions;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.lateCount = lateCount;
        this.attendancePercentage = attendancePercentage;
    }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public int getLateCount() { return lateCount; }
    public void setLateCount(int lateCount) { this.lateCount = lateCount; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }
}