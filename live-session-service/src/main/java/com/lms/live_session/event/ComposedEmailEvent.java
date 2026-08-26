package com.lms.live_session.event;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Published when a user sends an email via the Email Management System.
 * Consumed by notification-service, which is responsible for the actual
 * delivery (SMTP/SendGrid/etc). One event represents ONE send and carries
 * ALL recipients combined — it is not fanned out into one event per
 * recipient.
 *
 * Kept separate from SessionNotificationEvent since this has no
 * session/batch/schedule concept and shouldn't carry those unused fields.
 */
public class ComposedEmailEvent {

    private Long emailId;
    private String fromEmail;
    private List<String> toEmails;
    private List<String> ccEmails;
    private List<String> bccEmails;
    private String subject;
    private String body;
    private String creatorId;
    private LocalDateTime timestamp;

    public ComposedEmailEvent() {
    }

    public ComposedEmailEvent(Long emailId, String fromEmail, List<String> toEmails,
                               List<String> ccEmails, List<String> bccEmails,
                               String subject, String body, String creatorId) {
        this.emailId = emailId;
        this.fromEmail = fromEmail;
        this.toEmails = toEmails;
        this.ccEmails = ccEmails;
        this.bccEmails = bccEmails;
        this.subject = subject;
        this.body = body;
        this.creatorId = creatorId;
        this.timestamp = LocalDateTime.now();
    }

    public Long getEmailId() {
        return emailId;
    }

    public void setEmailId(Long emailId) {
        this.emailId = emailId;
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

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}