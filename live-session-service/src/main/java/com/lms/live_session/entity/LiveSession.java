package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "live_sessions")
public class LiveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    private Long trainerId;
    private Long batchId;

    private String status;

    private LocalDate scheduledDate;
    private LocalTime scheduledTime;

    private Integer duration;

    private Boolean chatEnabled;
    private Boolean autoRecord;
    private Boolean notifyStudents;

    public LiveSession() {}

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Long getTrainerId() { return trainerId; }

    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public Long getBatchId() { return batchId; }

    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public LocalDate getScheduledDate() { return scheduledDate; }

    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalTime getScheduledTime() { return scheduledTime; }

    public void setScheduledTime(LocalTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public Integer getDuration() { return duration; }

    public void setDuration(Integer duration) { this.duration = duration; }

    public Boolean getChatEnabled() { return chatEnabled; }

    public void setChatEnabled(Boolean chatEnabled) { this.chatEnabled = chatEnabled; }

    public Boolean getAutoRecord() { return autoRecord; }

    public void setAutoRecord(Boolean autoRecord) { this.autoRecord = autoRecord; }

    public Boolean getNotifyStudents() { return notifyStudents; }

    public void setNotifyStudents(Boolean notifyStudents) { this.notifyStudents = notifyStudents; }
}