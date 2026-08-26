package com.lms.live_session.dto;

public class ScheduleRequestDTO {

    private String title;
    private String date;       // yyyy-MM-dd
    private String startTime;  // HH:mm
    private String endTime;    // HH:mm
    private String type;       // SESSION/CLASS/MEETING/TASK/PERSONAL
    private String location;
    private String description;
    private String reminder;
    private String creatorName;
    private Long organizationId;

    public ScheduleRequestDTO() {}

    public ScheduleRequestDTO(String title, String date, String startTime, String endTime, String type,
                               String location, String description, String reminder,
                               String creatorName, Long organizationId) {
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.location = location;
        this.description = description;
        this.reminder = reminder;
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

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReminder() { return reminder; }
    public void setReminder(String reminder) { this.reminder = reminder; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}