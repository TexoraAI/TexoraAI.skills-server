package com.lms.live_session.dto;

import java.time.LocalDateTime;

public class ReminderResponseDTO {

    private Long id;
    private Long eventId;
    private Long scheduleId;
    private String reminderTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private String linkedTitle;
    private String linkedDate;
    private String linkedStartTime;
    public ReminderResponseDTO() {
    }

    public ReminderResponseDTO(Long id, Long eventId, Long scheduleId, String reminderTime, String status,
                                LocalDateTime createdAt, LocalDateTime sentAt) {
        this.id = id;
        this.eventId = eventId;
        this.scheduleId = scheduleId;
        this.reminderTime = reminderTime;
        this.status = status;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    public String getLinkedTitle() {
        return linkedTitle;
    }

    public void setLinkedTitle(String linkedTitle) {
        this.linkedTitle = linkedTitle;
    }

    public String getLinkedDate() {
        return linkedDate;
    }

    public void setLinkedDate(String linkedDate) {
        this.linkedDate = linkedDate;
    }

    public String getLinkedStartTime() {
        return linkedStartTime;
    }

    public void setLinkedStartTime(String linkedStartTime) {
        this.linkedStartTime = linkedStartTime;
    }
}