package com.lms.live_session.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageDTO1 {
    private String senderName;
    private String senderIdentity;
    private String text;
    private String sentAt;

    public ChatMessageDTO1() {}

    public ChatMessageDTO1(String senderName, String senderIdentity, String text, String sentAt) {
        this.senderName = senderName;
        this.senderIdentity = senderIdentity;
        this.text = text;
        this.sentAt = sentAt;
    }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderIdentity() { return senderIdentity; }
    public void setSenderIdentity(String senderIdentity) { this.senderIdentity = senderIdentity; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}