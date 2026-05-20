package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "live_sessions")
public class LiveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    private Long batchId;
    private String status;

    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private Integer duration;

    // ✅ Tracks when the trainer actually clicked "Go Live"
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    // ✅ Tracks when session was created (used by frontend 15-min window logic)
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ✅ Tracks when session actually ended
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    private Boolean chatEnabled;
    private Boolean autoRecord;
    private Boolean notifyStudents;

    // ✅ Viewer count (updated by participant join/leave)
    @Column(name = "viewer_count", columnDefinition = "int default 0")
    private Integer viewerCount = 0;
    
    @Column(name = "egress_id")
    private String egressId;

    @Column(name = "recording_s3_url")
    private String recordingS3Url;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) this.status = "SCHEDULED";
        this.createdAt = LocalDateTime.now();
    }
    
 // ── ADD these 3 fields to LiveSession entity ──────────────────

//    @Column(name = "meeting_type")  // "CUSTOM" | "EXTERNAL"
//    private String meetingType;
 // Change this field default
    @Column(name = "meeting_type")
    private String meetingType = "CUSTOM";  // ADD default here
    
    
    @Column(name = "external_meeting_url")
    private String externalMeetingUrl;

    @Column(name = "is_published", columnDefinition = "boolean default false")
    private Boolean isPublished = false;
    
    //new for time zone 
    @Column
    private String timezone;

    // ── ADD Getters & Setters ──────────────────────────────────────

   

    public LiveSession() {}

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

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

    public LocalDateTime getActualStartTime() { return actualStartTime; }
    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getActualEndTime() { return actualEndTime; }
    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
    }

    public Boolean getChatEnabled() { return chatEnabled; }
    public void setChatEnabled(Boolean chatEnabled) { this.chatEnabled = chatEnabled; }
    
 // getters and setters
    public String getEgressId() { return egressId; }
    public void setEgressId(String egressId) { this.egressId = egressId; }
    public String getRecordingS3Url() { return recordingS3Url; }
    public void setRecordingS3Url(String url) { this.recordingS3Url = url; }

    public Boolean getAutoRecord() { return autoRecord; }
    public void setAutoRecord(Boolean autoRecord) { this.autoRecord = autoRecord; }

    public Boolean getNotifyStudents() { return notifyStudents; }
    public void setNotifyStudents(Boolean notifyStudents) { this.notifyStudents = notifyStudents; }

    public Integer getViewerCount() { return viewerCount; }
    public void setViewerCount(Integer viewerCount) { this.viewerCount = viewerCount; }
    
    public String getMeetingType() { return meetingType; }
    public void setMeetingType(String meetingType) { this.meetingType = meetingType; }

    public String getExternalMeetingUrl() { return externalMeetingUrl; }
    public void setExternalMeetingUrl(String url) { this.externalMeetingUrl = url; }

    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    
 // ADD getter/setter
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

}