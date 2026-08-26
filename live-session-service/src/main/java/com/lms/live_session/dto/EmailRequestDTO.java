package com.lms.live_session.dto;

import java.util.List;

public class EmailRequestDTO {

    private String subject;
    private String body;
    private List<String> toEmails;
    private List<String> ccEmails;
    private List<String> bccEmails;

    public EmailRequestDTO() {
    }

    public EmailRequestDTO(String subject, String body, List<String> toEmails,
                            List<String> ccEmails, List<String> bccEmails) {
        this.subject = subject;
        this.body = body;
        this.toEmails = toEmails;
        this.ccEmails = ccEmails;
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
}