// dto/RecordingUploadRequest.java
package com.lms.live_session.dto;

public class RecordingUploadRequest {
    private Long sessionId;
    private Long batchId;
    private Long trainerId;
    private String title;
    private String description;
    private String batchName;
    private Integer durationMinutes;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
}