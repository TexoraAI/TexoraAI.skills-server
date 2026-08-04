package com.lms.live_session.dto;

public class TexoraMeetingRequestDTO {
    private String topic;
    private String startTime;      // ISO 8601 UTC, e.g. 2026-08-12T14:30:00Z
    private Integer durationMinutes;

    public TexoraMeetingRequestDTO() {}

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}