package com.lms.progress.dto;

import com.lms.progress.model.ResourceType;

import java.time.LocalDateTime;

public class OrgResourceResponse {

    private Long id;
    private String orgId;
    private Long nodeId;
    private ResourceType type;
    private String title;
    private String url;
    private String description;
    private Integer durationMinutes;
    private String difficulty;
    private Integer upvotes;
    private boolean isFeatured;
    private Long addedBy;
    private LocalDateTime createdAt;

    public OrgResourceResponse() {
    }

    public OrgResourceResponse(Long id, String orgId, Long nodeId, ResourceType type, String title, String url,
                                String description, Integer durationMinutes, String difficulty, Integer upvotes,
                                boolean isFeatured, Long addedBy, LocalDateTime createdAt) {
        this.id = id;
        this.orgId = orgId;
        this.nodeId = nodeId;
        this.type = type;
        this.title = title;
        this.url = url;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.difficulty = difficulty;
        this.upvotes = upvotes;
        this.isFeatured = isFeatured;
        this.addedBy = addedBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
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

    public Integer getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Integer upvotes) {
        this.upvotes = upvotes;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public Long getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(Long addedBy) {
        this.addedBy = addedBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}