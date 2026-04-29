package com.lms.chat.dto;

import com.lms.chat.entity.NotebookSource;
import java.time.LocalDateTime;

public class NotebookSourceResponse {

    private Long id;
    private String sourceType;
    private String title;
    private String url;
    private LocalDateTime createdAt;

    public static NotebookSourceResponse from(NotebookSource s) {
        NotebookSourceResponse r = new NotebookSourceResponse();
        r.id         = s.getId();
        r.sourceType = s.getSourceType().name();
        r.title      = s.getTitle();
        r.url        = s.getUrl();
        r.createdAt  = s.getCreatedAt();
        return r;
    }

    // ===== Getters =====
    public Long getId() {
        return id;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ===== Setters =====
    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}