package com.lms.progress.dto;

public class CloneTemplateRequest {

    private Long sourceTemplateId;
    private String orgId;
    private String title;
    private String slug;
    private String description;
    private String category;
    private String thumbnailUrl;

    public CloneTemplateRequest() {
    }

    public CloneTemplateRequest(Long sourceTemplateId, String orgId, String title, String slug, String description,
                                 String category, String thumbnailUrl) {
        this.sourceTemplateId = sourceTemplateId;
        this.orgId = orgId;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Long getSourceTemplateId() {
        return sourceTemplateId;
    }

    public void setSourceTemplateId(Long sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
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
}