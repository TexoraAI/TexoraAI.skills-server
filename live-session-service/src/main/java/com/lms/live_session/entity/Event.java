package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, length = 20)
    private String mode; // ONLINE or IN_PERSON

    @Column(length = 512)
    private String location; // nullable for online events

    @Column(length = 1000)
    private String description;
    @Column(length = 255)
    private String purpose; // preset label or free-text custom purpose
    @Column(length = 20)
    private String reminder; // NO_REMINDER / 5MIN / 10MIN / 30MIN / 1HOUR / 1DAY

    @Column(length = 20)
    private String repeat; // DOES_NOT_REPEAT / DAILY / WEEKLY / MONTHLY / CUSTOM

    @Column(length = 20)
    private String availability; // BUSY / FREE / TENTATIVE / OUT_OF_OFFICE
    
    @Column(length = 20, nullable = false)
    private String status = "ACTIVE"; // ACTIVE / CANCELLED

    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    @Column(name = "creator_role", nullable = false, length = 32)
    private String creatorRole;

    @Column(name = "creator_name", nullable = false)
    private String creatorName;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "meeting_id")
    private Long meetingId; // FK to Meeting, only set when mode = ONLINE

    // Added for Google Calendar sync: lets syncNow() detect an event it already
    // imported (vs. creating a duplicate) on every hourly re-sync. Nullable/unique -
    // events created natively in the app (not from Google) leave this null.
    @Column(name = "google_event_id", unique = true)
    private String googleEventId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "waiting_room", nullable = false)
    private Boolean waitingRoom = false;

    @Column(name = "mute_on_entry", nullable = false)
    private Boolean muteOnEntry = false;

    @Column(name = "record_meeting", nullable = false)
    private Boolean recordMeeting = false;

    @Column(name = "allow_screen_share", nullable = false)
    private Boolean allowScreenShare = true;

    public Event() {}

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public String getReminder() { return reminder; }
    public void setReminder(String reminder) { this.reminder = reminder; }

    public String getRepeat() { return repeat; }
    public void setRepeat(String repeat) { this.repeat = repeat; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    public String getGoogleEventId() { return googleEventId; }
    public void setGoogleEventId(String googleEventId) { this.googleEventId = googleEventId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public Boolean getWaitingRoom() { return waitingRoom; }
    public void setWaitingRoom(Boolean waitingRoom) { this.waitingRoom = waitingRoom; }

    public Boolean getMuteOnEntry() { return muteOnEntry; }
    public void setMuteOnEntry(Boolean muteOnEntry) { this.muteOnEntry = muteOnEntry; }

    public Boolean getRecordMeeting() { return recordMeeting; }
    public void setRecordMeeting(Boolean recordMeeting) { this.recordMeeting = recordMeeting; }

    public Boolean getAllowScreenShare() { return allowScreenShare; }
    public void setAllowScreenShare(Boolean allowScreenShare) { this.allowScreenShare = allowScreenShare; }
}