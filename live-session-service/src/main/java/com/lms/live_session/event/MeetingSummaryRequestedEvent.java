package com.lms.live_session.event;

import com.lms.live_session.dto.ChatMessageDTO1;
import java.time.LocalDateTime;
import java.util.List;

public class MeetingSummaryRequestedEvent {
    private Long meetingId;
    private String title;
    private String creatorId;
    private String creatorRole;
    private Long organizationId;
    private LocalDateTime endedAt;
    private String recordingS3Url;
    private String requestedByEmail;
    private String requestedByRole;
    private List<ChatMessageDTO1> chatMessages;

    public MeetingSummaryRequestedEvent() {}

    public MeetingSummaryRequestedEvent(Long meetingId, String title, String creatorId, String creatorRole,
                                         Long organizationId, LocalDateTime endedAt, String recordingS3Url,
                                         String requestedByEmail, String requestedByRole,
                                         List<ChatMessageDTO1> chatMessages) {
        this.meetingId = meetingId;
        this.title = title;
        this.creatorId = creatorId;
        this.creatorRole = creatorRole;
        this.organizationId = organizationId;
        this.endedAt = endedAt;
        this.recordingS3Url = recordingS3Url;
        this.requestedByEmail = requestedByEmail;
        this.requestedByRole = requestedByRole;
        this.chatMessages = chatMessages;
    }

    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorRole() { return creatorRole; }
    public void setCreatorRole(String creatorRole) { this.creatorRole = creatorRole; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public String getRecordingS3Url() { return recordingS3Url; }
    public void setRecordingS3Url(String recordingS3Url) { this.recordingS3Url = recordingS3Url; }

    public String getRequestedByEmail() { return requestedByEmail; }
    public void setRequestedByEmail(String requestedByEmail) { this.requestedByEmail = requestedByEmail; }

    public String getRequestedByRole() { return requestedByRole; }
    public void setRequestedByRole(String requestedByRole) { this.requestedByRole = requestedByRole; }

    public List<ChatMessageDTO1> getChatMessages() { return chatMessages; }
    public void setChatMessages(List<ChatMessageDTO1> chatMessages) { this.chatMessages = chatMessages; }
}