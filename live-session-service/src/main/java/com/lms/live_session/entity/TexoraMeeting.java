package com.lms.live_session.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "texora_meetings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_texora_meetings_texora_meeting_id", columnNames = "texora_meeting_id"),
        @UniqueConstraint(name = "uk_texora_meetings_join_code", columnNames = "join_code")
})
public class TexoraMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "texora_meeting_id", nullable = false, updatable = false, length = 64)
    private String texoraMeetingId;

    @Column(name = "external_ref", length = 64)
    private String externalRef;

    @Column(nullable = false)
    private String topic;

    @Column(name = "start_time_utc", nullable = false)
    private LocalDateTime startTimeUtc;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "expires_at_utc")
    private LocalDateTime expiresAtUtc;

    @Column(name = "join_code", nullable = false, updatable = false, length = 32)
    private String joinCode;

    @Column(name = "room_name", nullable = false, length = 64)
    private String roomName;

    @Column(name = "meeting_link", nullable = false, length = 512)
    private String meetingLink;

    @Column(name = "context_json", columnDefinition = "TEXT")
    private String contextJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private TexoraMeetingStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "egress_id")
    private String egressId;

    @Column(name = "recording_s3_url", length = 1024)
    private String recordingS3Url;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = TexoraMeetingStatus.SCHEDULED;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public TexoraMeeting() {}

    public Long getId() { return id; }

    public String getTexoraMeetingId() { return texoraMeetingId; }
    public void setTexoraMeetingId(String texoraMeetingId) { this.texoraMeetingId = texoraMeetingId; }

    public String getExternalRef() { return externalRef; }
    public void setExternalRef(String externalRef) { this.externalRef = externalRef; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public LocalDateTime getStartTimeUtc() { return startTimeUtc; }
    public void setStartTimeUtc(LocalDateTime startTimeUtc) { this.startTimeUtc = startTimeUtc; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public LocalDateTime getExpiresAtUtc() { return expiresAtUtc; }
    public void setExpiresAtUtc(LocalDateTime expiresAtUtc) { this.expiresAtUtc = expiresAtUtc; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getContextJson() { return contextJson; }
    public void setContextJson(String contextJson) { this.contextJson = contextJson; }

    public TexoraMeetingStatus getStatus() { return status; }
    public void setStatus(TexoraMeetingStatus status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getEgressId() { return egressId; }
    public void setEgressId(String egressId) { this.egressId = egressId; }

    public String getRecordingS3Url() { return recordingS3Url; }
    public void setRecordingS3Url(String recordingS3Url) { this.recordingS3Url = recordingS3Url; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
