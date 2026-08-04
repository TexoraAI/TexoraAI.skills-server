package com.lms.live_session.dto;

public class TexoraMeetingResponseDTO {
    private String meetingLink;
    private String meetingId;
    private String expiresAt; // ISO 8601 UTC, optional

    public TexoraMeetingResponseDTO() {}

    public TexoraMeetingResponseDTO(String meetingLink, String meetingId, String expiresAt) {
        this.meetingLink = meetingLink;
        this.meetingId = meetingId;
        this.expiresAt = expiresAt;
    }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}