package com.lms.progress.dto;

public class UpdateTemplateRequest {

    private String title;
    private String description;
    private String category;
    private String thumbnailUrl;
    private Boolean isPublished;
    private Boolean isArchived;

    public UpdateTemplateRequest() {
    }

    public UpdateTemplateRequest(String title, String description, String category, String thumbnailUrl,
                                  Boolean isPublished, Boolean isArchived) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.isPublished = isPublished;
        this.isArchived = isArchived;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public Boolean getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(Boolean isArchived) {
        this.isArchived = isArchived;
    }
}
