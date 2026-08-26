package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "roadmap_template")
public class RoadmapTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "total_nodes", nullable = false)
    private Integer totalNodes;

    @Column(name = "is_archived", nullable = false)
    private boolean isArchived;

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

    public RoadmapTemplate() {
    }

    public RoadmapTemplate(Long id, String title, String slug, String description, String category,
                            String thumbnailUrl, boolean isPublished, Integer version, Integer totalNodes,
                            boolean isArchived, Long createdBy, Long publishedBy, LocalDateTime publishedAt,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
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
