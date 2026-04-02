package com.lms.notification.dto;
import java.time.Instant;
import java.util.List;

public class NotificationDTO {
    private Long id;
    private String type;
    private String title;
    private String message;
    private boolean read;
    private Instant createdAt;
    private List<String> targetUserIds; // specific users
    private String targetRole;          // OR broadcast role

    public NotificationDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public List<String> getTargetUserIds() { return targetUserIds; }
    public void setTargetUserIds(List<String> t) { this.targetUserIds = t; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
}