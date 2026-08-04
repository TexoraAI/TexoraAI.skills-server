package com.lms.live_session.dto;

import java.util.List;

public class MeetingSummaryRequestDTO {
    private List<ChatMessageDTO1> messages;

    public List<ChatMessageDTO1> getMessages() { return messages; }
    public void setMessages(List<ChatMessageDTO1> messages) { this.messages = messages; }
}