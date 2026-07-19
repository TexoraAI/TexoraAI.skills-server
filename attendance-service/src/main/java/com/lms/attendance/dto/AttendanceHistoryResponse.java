package com.lms.attendance.dto;

import java.util.List;

public class AttendanceHistoryResponse {

    private List<AttendanceRecordResponse> records;
    private AttendanceAnalyticsResponse analytics;

    public AttendanceHistoryResponse() {}

    public AttendanceHistoryResponse(List<AttendanceRecordResponse> records, AttendanceAnalyticsResponse analytics) {
        this.records = records;
        this.analytics = analytics;
    }

    public List<AttendanceRecordResponse> getRecords() { return records; }
    public void setRecords(List<AttendanceRecordResponse> records) { this.records = records; }

    public AttendanceAnalyticsResponse getAnalytics() { return analytics; }
    public void setAnalytics(AttendanceAnalyticsResponse analytics) { this.analytics = analytics; }
}
