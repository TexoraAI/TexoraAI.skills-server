// NotebookPageResponse.java
package com.lms.chat.dto;

import com.lms.chat.entity.NotebookPage;
import java.time.LocalDateTime;

public class NotebookPageResponse {
    private Long id;
    private String title;
    private String content;
    private int position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotebookPageResponse from(NotebookPage p) {
        NotebookPageResponse r = new NotebookPageResponse();
        r.id        = p.getId();
        r.title     = p.getTitle();
        r.content   = p.getContent();
        r.position  = p.getPosition();
        r.createdAt = p.getCreatedAt();
        r.updatedAt = p.getUpdatedAt();
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}