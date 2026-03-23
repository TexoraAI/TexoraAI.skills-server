package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private Long senderId;
    private String senderRole;

    private String message;

    private LocalDateTime timestamp;

    public ChatMessage(){}

    public Long getId() { return id; }

    public Long getSessionId() { return sessionId; }

    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getSenderId() { return senderId; }

    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderRole() { return senderRole; }

    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}