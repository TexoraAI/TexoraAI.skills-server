package com.lms.live_session.dto;
import java.util.List;
public class MeetingRequestDTO {

    private String title;

    // Required only for /scheduled — format: yyyy-MM-dd
    private String date;

    // Required only for /scheduled — format: HH:mm
    private String time;

    // Browser timezone, e.g. "Asia/Kolkata". Required for /scheduled.
    private String timezone;

    // Ownership only — optional
    private Long organizationId;

    // Display name of the creator, sent once by the dashboard at creation
    // time (already known client-side from the logged-in user's profile).
    // The JWT carries no "name" claim, so this cannot be derived server-side.
    private String creatorName;
    
    private List<String> participantEmails;
    
    

    public MeetingRequestDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    
    public List<String> getParticipantEmails() { return participantEmails; }
    public void setParticipantEmails(List<String> participantEmails) { this.participantEmails = participantEmails; }
}