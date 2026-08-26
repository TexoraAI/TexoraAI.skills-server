package com.lms.progress.dto;

public class EnrolledRoadmapSummary {

    private Long orgRoadmapId;
    private String orgId;
    private String slug;
    private String title;
    private String thumbnailUrl;
    private Double completionPercent;

    public EnrolledRoadmapSummary() {
    }

    public EnrolledRoadmapSummary(Long orgRoadmapId, String orgId, String slug, String title, String thumbnailUrl,
                                   Double completionPercent) {
        this.orgRoadmapId = orgRoadmapId;
        this.orgId = orgId;
        this.slug = slug;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.completionPercent = completionPercent;
    }

    public Long getOrgRoadmapId() {
        return orgRoadmapId;
    }

    public void setOrgRoadmapId(Long orgRoadmapId) {
        this.orgRoadmapId = orgRoadmapId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Double getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(Double completionPercent) {
        this.completionPercent = completionPercent;
    }
}