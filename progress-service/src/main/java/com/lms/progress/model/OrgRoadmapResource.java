package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "org_roadmap_resource")
public class OrgRoadmapResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ResourceType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "difficulty")
    private String difficulty;

    @Column(name = "upvotes", nullable = false)
    private Integer upvotes;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured;

    @Column(name = "added_by", nullable = false)
    private Long addedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public OrgRoadmapResource() {
    }

    public OrgRoadmapResource(Long id, Long nodeId, ResourceType type, String title, String url,
                               String description, Integer durationMinutes, String difficulty,
                               Integer upvotes, boolean isFeatured, Long addedBy, LocalDateTime createdAt) {
        this.id = id;
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
