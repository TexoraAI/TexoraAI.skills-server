package com.lms.progress.dto;

import java.time.LocalDateTime;

/**
 * Not in the original spec DTO list - added because section 8 defines
 * GET /mentor/{syllabusId}/history as an endpoint, but no response shape was
 * specified for it. Minimal projection of RoadmapUpgradedMentorMessage.
 */
public class RoadmapUpgradedMentorMessageDto {

    private Long id;
    private String sender;
    private String messageText;
    private LocalDateTime sentAt;

    public RoadmapUpgradedMentorMessageDto() {
    }

    public RoadmapUpgradedMentorMessageDto(Long id, String sender, String messageText, LocalDateTime sentAt) {
        this.id = id;
        this.sender = sender;
        this.messageText = messageText;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
