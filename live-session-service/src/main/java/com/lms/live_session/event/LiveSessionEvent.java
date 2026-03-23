package com.lms.live_session.event;

import java.time.LocalDateTime;

public class LiveSessionEvent {

    private Long sessionId;
    private Long batchId;
    private Long trainerId;
    private String status;
    private String timestamp;

    public LiveSessionEvent() {}

    public LiveSessionEvent(Long sessionId, Long batchId, Long trainerId, String status) {
        this.sessionId = sessionId;
        this.batchId = batchId;
        this.trainerId = trainerId;
        this.status = status;
        this.timestamp = timestamp;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}