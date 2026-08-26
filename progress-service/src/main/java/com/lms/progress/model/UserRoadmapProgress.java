package com.lms.progress.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_roadmap_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_roadmap_progress_user_node",
                columnNames = {"user_id", "node_id"}
        )
)
public class UserRoadmapProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * Nullable: mirrors the parent OrgRoadmap's orgId. Null means the
     * progress row belongs to a null-org roadmap administered by
     * SUPER_ADMIN rather than an ADMIN/TENANT_ADMIN.
     */
    @Column(name = "org_id")
    private String orgId;

    @Column(name = "org_roadmap_id", nullable = false)
    private Long orgRoadmapId;

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NodeStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @Column(name = "time_spent_minutes", nullable = false)
    private Integer timeSpentMinutes;

    @Column(name = "resource_clicks", nullable = false)
    private Integer resourceClicks;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserRoadmapProgress() {
    }

    public UserRoadmapProgress(Long id, Long userId, String orgId, Long orgRoadmapId, Long nodeId,
                                NodeStatus status, LocalDateTime completedAt, LocalDateTime lastAccessedAt,
                                Integer timeSpentMinutes, Integer resourceClicks,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.orgId = orgId;
        this.orgRoadmapId = orgRoadmapId;
        this.nodeId = nodeId;
        this.status = status;
        this.completedAt = completedAt;
        this.lastAccessedAt = lastAccessedAt;
        this.timeSpentMinutes = timeSpentMinutes;
        this.resourceClicks = resourceClicks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Long getOrgRoadmapId() {
        return orgRoadmapId;
    }

    public void setOrgRoadmapId(Long orgRoadmapId) {
        this.orgRoadmapId = orgRoadmapId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public Integer getTimeSpentMinutes() {
        return timeSpentMinutes;
    }

    public void setTimeSpentMinutes(Integer timeSpentMinutes) {
        this.timeSpentMinutes = timeSpentMinutes;
    }

    public Integer getResourceClicks() {
        return resourceClicks;
    }

    public void setResourceClicks(Integer resourceClicks) {
        this.resourceClicks = resourceClicks;
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