package com.lms.progress.dto;

public class RoadmapListItemResponse {

    private Long id;
    private String orgId;
    private String slug;
    private String title;
    private String category;
    private String thumbnailUrl;
    private Integer totalNodes;

    public RoadmapListItemResponse() {
    }

    public RoadmapListItemResponse(Long id, String orgId, String slug, String title, String category,
                                    String thumbnailUrl, Integer totalNodes) {
        this.id = id;
        this.orgId = orgId;
        this.slug = slug;
        this.title = title;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.totalNodes = totalNodes;
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

    public Integer getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(Integer totalNodes) {
        this.totalNodes = totalNodes;
    }
}