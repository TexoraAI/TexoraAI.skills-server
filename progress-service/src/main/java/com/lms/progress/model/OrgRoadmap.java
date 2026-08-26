package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_roadmap")
public class OrgRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Nullable: when null, this roadmap belongs to the "no org" pool that is
     * administered directly by SUPER_ADMIN instead of an ADMIN/TENANT_ADMIN.
     */
    @Column(name = "org_id")
    private String orgId;

    @Column(name = "source_template_id")
    private Long sourceTemplateId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

    @Column(name = "total_nodes", nullable = false)
    private Integer totalNodes;

    @Column(name = "total_students", nullable = false)
    private Integer totalStudents;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "published_by")
    private Long publishedBy;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public OrgRoadmap() {
    }

    public OrgRoadmap(Long id, String orgId, Long sourceTemplateId, String title, String slug, String description,
                       String category, String thumbnailUrl, boolean isPublished, boolean isArchived,
                       Integer totalNodes, Integer totalStudents, Long createdBy, Long publishedBy,
                       LocalDateTime publishedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.orgId = orgId;
        this.sourceTemplateId = sourceTemplateId;
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.category = category;
        this.thumbnailUrl = thumbnailUrl;
        this.isPublished = isPublished;
        this.isArchived = isArchived;
        this.totalNodes = totalNodes;
        this.totalStudents = totalStudents;
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

    public Long getSourceTemplateId() {
        return sourceTemplateId;
    }

    public void setSourceTemplateId(Long sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
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

    public boolean isArchived() {
        return isArchived;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
    }

    public Integer getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(Integer totalNodes) {
        this.totalNodes = totalNodes;
    }

    public Integer getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(Integer totalStudents) {
        this.totalStudents = totalStudents;
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