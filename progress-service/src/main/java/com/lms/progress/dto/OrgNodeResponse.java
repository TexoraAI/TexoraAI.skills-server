package com.lms.progress.dto;

import com.lms.progress.model.NodeType;

import java.time.LocalDateTime;
import java.util.List;

public class OrgNodeResponse {

    private Long id;
    private String orgId;
    private Long orgRoadmapId;
    private Long sourceNodeId;
    private String title;
    private String description;
    private NodeType type;
    private Double positionX;
    private Double positionY;
    private boolean isOptional;
    private Integer estimatedHours;
    private Integer orderIndex;
    private boolean hasQuiz;
    private boolean hasProject;
    private List<Long> parentNodeIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrgNodeResponse() {
    }

    public OrgNodeResponse(Long id, String orgId, Long orgRoadmapId, Long sourceNodeId, String title,
                            String description, NodeType type, Double positionX, Double positionY,
                            boolean isOptional, Integer estimatedHours, Integer orderIndex, boolean hasQuiz,
                            boolean hasProject, List<Long> parentNodeIds, LocalDateTime createdAt,
                            LocalDateTime updatedAt) {
        this.id = id;
        this.orgId = orgId;
        this.orgRoadmapId = orgRoadmapId;
        this.sourceNodeId = sourceNodeId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.positionX = positionX;
        this.positionY = positionY;
        this.isOptional = isOptional;
        this.estimatedHours = estimatedHours;
        this.orderIndex = orderIndex;
        this.hasQuiz = hasQuiz;
        this.hasProject = hasProject;
        this.parentNodeIds = parentNodeIds;
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

    public Long getOrgRoadmapId() {
        return orgRoadmapId;
    }

    public void setOrgRoadmapId(Long orgRoadmapId) {
        this.orgRoadmapId = orgRoadmapId;
    }

    public Long getSourceNodeId() {
        return sourceNodeId;
    }

    public void setSourceNodeId(Long sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
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

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Double getPositionX() {
        return positionX;
    }

    public void setPositionX(Double positionX) {
        this.positionX = positionX;
    }

    public Double getPositionY() {
        return positionY;
    }

    public void setPositionY(Double positionY) {
        this.positionY = positionY;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isHasQuiz() {
        return hasQuiz;
    }

    public void setHasQuiz(boolean hasQuiz) {
        this.hasQuiz = hasQuiz;
    }

    public boolean isHasProject() {
        return hasProject;
    }

    public void setHasProject(boolean hasProject) {
        this.hasProject = hasProject;
    }

    public List<Long> getParentNodeIds() {
        return parentNodeIds;
    }

    public void setParentNodeIds(List<Long> parentNodeIds) {
        this.parentNodeIds = parentNodeIds;
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