package com.lms.live_session.dto;

public class ChatMessageDTO {

    private Long sessionId;
    private Long senderId;
    private String role;
    private String message;

    public Long getSessionId() { return sessionId; }

    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getSenderId() { return senderId; }

    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }
}