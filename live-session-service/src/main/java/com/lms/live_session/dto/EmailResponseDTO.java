package com.lms.live_session.dto;

import java.time.LocalDateTime;
import java.util.List;

public class EmailResponseDTO {

    private Long id;
    private String subject;
    private String body;
    private String fromEmail;
    private List<String> toEmails;
    private List<String> ccEmails;
    private List<String> bccEmails;
    private String status;
    private String creatorId;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public EmailResponseDTO() {
    }

    public EmailResponseDTO(Long id, String subject, String body, String fromEmail,
                             List<String> toEmails, List<String> ccEmails, List<String> bccEmails,
                             String status, String creatorId, LocalDateTime createdAt, LocalDateTime sentAt) {
        this.id = id;
        this.subject = subject;
        this.body = body;
        this.fromEmail = fromEmail;
        this.toEmails = toEmails;
        this.ccEmails = ccEmails;
        this.bccEmails = bccEmails;
        this.status = status;
        this.creatorId = creatorId;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public List<String> getToEmails() {
        return toEmails;
    }

    public void setToEmails(List<String> toEmails) {
        this.toEmails = toEmails;
    }

    public List<String> getCcEmails() {
        return ccEmails;
    }

    public void setCcEmails(List<String> ccEmails) {
        this.ccEmails = ccEmails;
    }

    public List<String> getBccEmails() {
        return bccEmails;
    }

    public void setBccEmails(List<String> bccEmails) {
        this.bccEmails = bccEmails;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}