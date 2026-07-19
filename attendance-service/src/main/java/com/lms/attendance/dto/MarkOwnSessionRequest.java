package com.lms.attendance.dto;

import java.time.LocalDate;

public class MarkOwnSessionRequest {

    private Long batchId;
    private LocalDate date;
    private String status; // "PRESENT" | "ABSENT"

    public MarkOwnSessionRequest() {}

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}