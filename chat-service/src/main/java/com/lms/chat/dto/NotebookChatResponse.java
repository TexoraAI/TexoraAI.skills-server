package com.lms.chat.dto;

public class NotebookChatResponse {
    private String reply;

    public NotebookChatResponse(String reply) { this.reply = reply; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}