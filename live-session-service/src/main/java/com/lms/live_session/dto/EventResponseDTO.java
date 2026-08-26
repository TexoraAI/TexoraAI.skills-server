package com.lms.live_session.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class EventResponseDTO {

    private Long id;
    private String title;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

    private String mode;
    private String location;
    private String description;
    private String purpose;
    private String reminder;
    private String repeat;
    private String availability;
    private String status;
    private String creatorId;
    private String creatorRole;
    private String creatorName;
    private Long organizationId;
    private Long meetingId;
    private String meetingUrl;
    private String joinCode;
    private List<EventAttendeeDTO> attendees;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    private Boolean waitingRoom;
    private Boolean muteOnEntry;
    private Boolean recordMeeting;
    private Boolean allowScreenShare;
    public EventResponseDTO() {}

    public EventResponseDTO(Long id, String title, LocalDate date, LocalTime startTime, LocalTime endTime,
                             String mode, String location, String description, String reminder, String repeat,
                             String availability, String creatorId, String creatorRole, String creatorName,
                             Long organizationId, Long meetingId, String meetingUrl, String joinCode,
                             List<EventAttendeeDTO> attendees, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.mode = mode;
        this.location = location;
        this.description = description;
        this.reminder = reminder;
        this.repeat = repeat;
        this.availability = availability;
        this.creatorId = creatorId;
        this.creatorRole = creatorRole;
        this.creatorName = creatorName;
        this.organizationId = organizationId;
        this.meetingId = meetingId;
        this.meetingUrl = meetingUrl;
        this.joinCode = joinCode;
        this.attendees = attendees;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getReminder() { return reminder; }
    public void setReminder(String reminder) { this.reminder = reminder; }

    public String getRepeat() { return repeat; }
    public void setRepeat(String repeat) { this.repeat = repeat; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

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

    public String getMeetingUrl() { return meetingUrl; }
    public void setMeetingUrl(String meetingUrl) { this.meetingUrl = meetingUrl; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public List<EventAttendeeDTO> getAttendees() { return attendees; }
    public void setAttendees(List<EventAttendeeDTO> attendees) { this.attendees = attendees; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }


public Boolean getWaitingRoom() { return waitingRoom; }
public void setWaitingRoom(Boolean waitingRoom) { this.waitingRoom = waitingRoom; }

public Boolean getMuteOnEntry() { return muteOnEntry; }
public void setMuteOnEntry(Boolean muteOnEntry) { this.muteOnEntry = muteOnEntry; }

public Boolean getRecordMeeting() { return recordMeeting; }
public void setRecordMeeting(Boolean recordMeeting) { this.recordMeeting = recordMeeting; }

public Boolean getAllowScreenShare() { return allowScreenShare; }
public void setAllowScreenShare(Boolean allowScreenShare) { this.allowScreenShare = allowScreenShare; }
public String getStatus() { return status; }
public void setStatus(String status) { this.status = status; }
public String getPurpose() { return purpose; }
public void setPurpose(String purpose) { this.purpose = purpose; }
}