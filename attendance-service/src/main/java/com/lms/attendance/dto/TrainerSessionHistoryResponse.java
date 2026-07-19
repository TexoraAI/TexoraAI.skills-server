package com.lms.attendance.dto;

import com.lms.attendance.entity.TrainerSessionAttendance;

import java.util.List;

public class TrainerSessionHistoryResponse {

    private List<TrainerSessionAttendance> records;
    private AttendanceAnalyticsResponse analytics;

    public TrainerSessionHistoryResponse() {}

    public TrainerSessionHistoryResponse(List<TrainerSessionAttendance> records, AttendanceAnalyticsResponse analytics) {
        this.records = records;
        this.analytics = analytics;
    }

    public List<TrainerSessionAttendance> getRecords() { return records; }
    public void setRecords(List<TrainerSessionAttendance> records) { this.records = records; }

    public AttendanceAnalyticsResponse getAnalytics() { return analytics; }
    public void setAnalytics(AttendanceAnalyticsResponse analytics) { this.analytics = analytics; }
}