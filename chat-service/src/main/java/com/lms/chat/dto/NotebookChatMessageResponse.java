package com.lms.chat.dto;

import com.lms.chat.entity.NotebookChatMessage;

import java.time.LocalDateTime;

public class NotebookChatMessageResponse {

    private Long id;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    public NotebookChatMessageResponse() {
    }

    public NotebookChatMessageResponse(Long id, String role, String content, LocalDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static NotebookChatMessageResponse from(NotebookChatMessage message) {
        return new NotebookChatMessageResponse(
                message.getId(),
                message.getRole().name(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}