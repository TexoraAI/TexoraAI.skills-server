package com.lms.live_session.event;

import java.time.LocalDateTime;

/**
 * Event for sending notifications to students and trainers
 * Produced by Live Session Service
 * Consumed by Notification Microservice
 */
public class SessionNotificationEvent {

    private Long sessionId;
    private String trainerEmail;          // ✅ FIXED (was Long trainerId)
    private Long batchId;
    private String sessionTitle;
    private String scheduledDate;         // "2026-05-10"
    private String scheduledTime;         // "14:30"
    private Integer durationMinutes;
    private String eventType;             // "STUDENT_REMINDER", "TRAINER_REMINDER", etc
    private String recipientEmail;
    private String recipientName;
    private String recipientRole;         // "STUDENT", "TRAINER", "PUBLIC_USER"
    private String sessionLink;           // Join URL
    private LocalDateTime timestamp;

    // ─────────────────────────────────────────────
    // Default Constructor
    // ─────────────────────────────────────────────
    public SessionNotificationEvent() {}

    // ─────────────────────────────────────────────
    // Main Constructor
    // ─────────────────────────────────────────────
    public SessionNotificationEvent(
            Long sessionId,
            String trainerEmail,           // ✅ FIXED
            Long batchId,
            String sessionTitle,
            String scheduledDate,
            String scheduledTime,
            Integer durationMinutes,
            String eventType,
            String recipientEmail,
            String recipientName,
            String recipientRole,
            String sessionLink
    ) {
        this.sessionId = sessionId;
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
        this.sessionTitle = sessionTitle;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.durationMinutes = durationMinutes;
        this.eventType = eventType;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
        this.recipientRole = recipientRole;
        this.sessionLink = sessionLink;
        this.timestamp = LocalDateTime.now();   // ✅ auto timestamp
    }

    // ─────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }

    public String getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(String scheduledDate) { this.scheduledDate = scheduledDate; }

    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientRole() { return recipientRole; }
    public void setRecipientRole(String recipientRole) { this.recipientRole = recipientRole; }

    public String getSessionLink() { return sessionLink; }
    public void setSessionLink(String sessionLink) { this.sessionLink = sessionLink; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}