package com.lms.progress.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Chat history for the AI Mentor, scoped to a syllabus so context persists per roadmap.
 */
@Entity
@Table(name = "roadmap_upgraded_mentor_message")
public class RoadmapUpgradedMentorMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long syllabusId;

    private Long userId;

    /**
     * "USER" or "MENTOR".
     */
    private String sender;

    @Column(columnDefinition = "TEXT")
    private String messageText;

    private LocalDateTime sentAt;

    public RoadmapUpgradedMentorMessage() {
    }

    public RoadmapUpgradedMentorMessage(Long id,
                                         Long syllabusId,
                                         Long userId,
                                         String sender,
                                         String messageText,
                                         LocalDateTime sentAt) {
        this.id = id;
        this.syllabusId = syllabusId;
        this.userId = userId;
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

    public Long getSyllabusId() {
        return syllabusId;
    }

    public void setSyllabusId(Long syllabusId) {
        this.syllabusId = syllabusId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
