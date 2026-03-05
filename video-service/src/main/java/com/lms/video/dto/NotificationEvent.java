package com.lms.video.dto;

import java.util.Map;

public class NotificationEvent {

    private String type;
    private Map<String, Object> payload;

    public NotificationEvent() {}

    public NotificationEvent(String type, Map<String, Object> payload) {
        this.type = type;
        this.payload = payload;
    }
//fvjkfv
    public String getType() { return type; }
    public Map<String, Object> getPayload() { return payload; }

    public void setType(String type) { this.type = type; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
