package com.lms.notification.event;

public class LiveSessionEvent {

    private Long sessionId;
    private Long batchId;
    private String type;

    public LiveSessionEvent() {}

    public LiveSessionEvent(Long sessionId, Long batchId, String type) {
        this.sessionId = sessionId;
        this.batchId = batchId;
        this.type = type;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public String getType() {
        return type;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public void setType(String type) {
        this.type = type;
    }
}