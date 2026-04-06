package com.lms.course.dto;

import java.util.Map;

public class ContentEvent {

    private String type;
    private Map<String, Object> payload;

    public ContentEvent() {}

    public ContentEvent(String type, Map<String, Object> payload) {
        this.type = type;
        this.payload = payload;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }

    @Override
    public String toString() {
        return "ContentEvent{type='" + type + "', payload=" + payload + "}";
    }
}