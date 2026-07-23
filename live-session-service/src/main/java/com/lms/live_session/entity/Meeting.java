package com.lms.live_session.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "meetings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_meetings_join_code", columnNames = "join_code")
})
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "join_code", nullable = false, updatable = false, length = 32)
    private String joinCode;

    @Column(name = "meeting_url", nullable = false, length = 512)
    private String meetingUrl;

    @Column(name = "room_name", nullable = false, length = 64)
    private String roomName;

    // Identity of the creator. Stored as email, same pattern LiveSession
    // uses for trainerEmail — there is no numeric userId available on
    // the Authentication principal in this project.
    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    // STUDENT | TRAINER | ADMIN | SUPER_ADMIN — extracted from the JWT
    // "role" claim server-side, not trusted from client input.
    @Column(name = "creator_role", nullable = false, length = 32)
    private String creatorRole;

    // Display name of the creator, for the public join page ("Host: ...").
    // JWT has no name claim, so this is supplied by the client at
    // creation time from the logged-in user's profile.
    @Column(name = "creator_name")
    private String creatorName;

    // Ownership info only — joining/authorization logic is implemented later.
    @Column(name = "organization_id")
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_type", nullable = false, length = 16)
    private MeetingType meetingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "meeting_status", nullable = false, length = 16)
    private MeetingStatus meetingStatus;

    // Browser timezone sent by the client, e.g. "Asia/Kolkata"
    @Column(length = 64)
    private String timezone;

    // Scheduled time normalized to UTC. Null for INSTANT meetings.
    @Column(name = "scheduled_time_utc")
    private LocalDateTime scheduledTimeUtc;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // Meeting is not deleted when it ends — the join code / room can be
    // reused for a future session by the same creator.
    @Column(nullable = false)
    private Boolean reusable = true;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.meetingStatus == null) {
            this.meetingStatus = MeetingStatus.CREATED;
        }
        if (this.reusable == null) {
            this.reusable = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Meeting() {}

    // ── Getters & Setters ──────────────────────────────────────

    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public MeetingType getMeetingType() { return meetingType; }
    public void setMeetingType(MeetingType meetingType) { this.meetingType = meetingType; }

    public MeetingStatus getMeetingStatus() { return meetingStatus; }
    public void setMeetingStatus(MeetingStatus meetingStatus) { this.meetingStatus = meetingStatus; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public LocalDateTime getScheduledTimeUtc() { return scheduledTimeUtc; }
    public void setScheduledTimeUtc(LocalDateTime scheduledTimeUtc) { this.scheduledTimeUtc = scheduledTimeUtc; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public Boolean getReusable() { return reusable; }
    public void setReusable(Boolean reusable) { this.reusable = reusable; }
}