package com.lms.live_session.dto;

import java.util.List;

public class EventRequestDTO {

    private String title;
    private String date;         // yyyy-MM-dd
    private String startTime;    // HH:mm
    private String endTime;      // HH:mm
    private String mode;         // ONLINE or IN_PERSON
    private String location;
    private String description;
    private String purpose;
    private String reminder;
    private String repeat;
    private String availability;
    private List<String> requiredAttendees;
    private List<String> optionalAttendees;
    private String creatorName;
    private Long organizationId;
    private Boolean waitingRoom;
    private Boolean muteOnEntry;
    private Boolean recordMeeting;
    private Boolean allowScreenShare;

    public EventRequestDTO() {}

    public EventRequestDTO(String title, String date, String startTime, String endTime, String mode,
                            String location, String description, String reminder, String repeat,
                            String availability, List<String> requiredAttendees, List<String> optionalAttendees,
                            String creatorName, Long organizationId) {
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
        this.requiredAttendees = requiredAttendees;
        this.optionalAttendees = optionalAttendees;
        this.creatorName = creatorName;
        this.organizationId = organizationId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

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

    public List<String> getRequiredAttendees() { return requiredAttendees; }
    public void setRequiredAttendees(List<String> requiredAttendees) { this.requiredAttendees = requiredAttendees; }

    public List<String> getOptionalAttendees() { return optionalAttendees; }
    public void setOptionalAttendees(List<String> optionalAttendees) { this.optionalAttendees = optionalAttendees; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    
    public Boolean getWaitingRoom() { return waitingRoom; }
    public void setWaitingRoom(Boolean waitingRoom) { this.waitingRoom = waitingRoom; }

    public Boolean getMuteOnEntry() { return muteOnEntry; }
    public void setMuteOnEntry(Boolean muteOnEntry) { this.muteOnEntry = muteOnEntry; }

    public Boolean getRecordMeeting() { return recordMeeting; }
    public void setRecordMeeting(Boolean recordMeeting) { this.recordMeeting = recordMeeting; }

    public Boolean getAllowScreenShare() { return allowScreenShare; }
    public void setAllowScreenShare(Boolean allowScreenShare) { this.allowScreenShare = allowScreenShare; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
}