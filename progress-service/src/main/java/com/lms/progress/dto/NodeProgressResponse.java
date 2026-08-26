package com.lms.progress.dto;

import com.lms.progress.model.NodeStatus;

import java.time.LocalDateTime;

public class NodeProgressResponse {

    private Long id;
    private String orgId;
    private Long userId;
    private Long orgRoadmapId;
    private Long nodeId;
    private NodeStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime lastAccessedAt;
    private Integer timeSpentMinutes;
    private Integer resourceClicks;

    public NodeProgressResponse() {
    }

    public NodeProgressResponse(Long id, String orgId, Long userId, Long orgRoadmapId, Long nodeId,
                                 NodeStatus status, LocalDateTime completedAt, LocalDateTime lastAccessedAt,
                                 Integer timeSpentMinutes, Integer resourceClicks) {
        this.id = id;
        this.orgId = orgId;
        this.userId = userId;
        this.orgRoadmapId = orgRoadmapId;
        this.nodeId = nodeId;
        this.status = status;
        this.completedAt = completedAt;
        this.lastAccessedAt = lastAccessedAt;
        this.timeSpentMinutes = timeSpentMinutes;
        this.resourceClicks = resourceClicks;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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
}