package com.lms.progress.dto;

import com.lms.progress.model.ResourceType;

public class UpdateOrgResourceRequest {

    private ResourceType type;
    private String title;
    private String url;
    private String description;
    private Integer durationMinutes;
    private String difficulty;
    private Boolean isFeatured;

    public UpdateOrgResourceRequest() {
    }

    public UpdateOrgResourceRequest(ResourceType type, String title, String url, String description,
                                     Integer durationMinutes, String difficulty, Boolean isFeatured) {
        this.type = type;
        this.title = title;
        this.url = url;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.difficulty = difficulty;
        this.isFeatured = isFeatured;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }
}
