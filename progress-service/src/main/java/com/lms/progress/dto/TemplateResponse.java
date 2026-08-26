package com.lms.progress.dto;

import java.time.LocalDateTime;

public class TemplateResponse {

    private Long id;
    private String orgId;
    private String title;
    private String slug;
    private String description;
    private String category;
    private String thumbnailUrl;
    private boolean isPublished;
    private Integer version;
    private Integer totalNodes;
    private boolean isArchived;
    private Long createdBy;
    private Long publishedBy;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TemplateResponse() {
    }

    public TemplateResponse(Long id, String orgId, String title, String slug, String description, String category,
                             String thumbnailUrl, boolean isPublished, Integer version, Integer totalNodes,
                             boolean isArchived, Long createdBy, Long publishedBy, LocalDateTime publishedAt,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orgId = orgId;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.isPublished = isPublished;
        this.version = version;
        this.totalNodes = totalNodes;
        this.isArchived = isArchived;
        this.createdBy = createdBy;
        this.publishedBy = publishedBy;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(Integer totalNodes) {
        this.totalNodes = totalNodes;
    }

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(Long publishedBy) {
        this.publishedBy = publishedBy;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}