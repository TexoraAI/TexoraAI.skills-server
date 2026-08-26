package com.lms.progress.dto;

import com.lms.progress.model.NodeStatus;
import com.lms.progress.model.NodeType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A single node within a RoadmapGraphResponse: node definition, its
 * resources, its parent edges, and the requesting student's progress
 * on that node (merged in, not a separate round trip).
 */
public class GraphNodeResponse {

    private Long id;
    private String orgId;
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
    private List<OrgResourceResponse> resources;
    private NodeStatus progressStatus;
    private LocalDateTime progressCompletedAt;
    private LocalDateTime progressLastAccessedAt;

    public GraphNodeResponse() {
    }

    public GraphNodeResponse(Long id, String orgId, String title, String description, NodeType type,
                              Double positionX, Double positionY, boolean isOptional, Integer estimatedHours,
                              Integer orderIndex, boolean hasQuiz, boolean hasProject, List<Long> parentNodeIds,
                              List<OrgResourceResponse> resources, NodeStatus progressStatus,
                              LocalDateTime progressCompletedAt, LocalDateTime progressLastAccessedAt) {
        this.id = id;
        this.orgId = orgId;
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
        this.resources = resources;
        this.progressStatus = progressStatus;
        this.progressCompletedAt = progressCompletedAt;
        this.progressLastAccessedAt = progressLastAccessedAt;
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

    public List<OrgResourceResponse> getResources() {
        return resources;
    }

    public void setResources(List<OrgResourceResponse> resources) {
        this.resources = resources;
    }

    public NodeStatus getProgressStatus() {
        return progressStatus;
    }

    public void setProgressStatus(NodeStatus progressStatus) {
        this.progressStatus = progressStatus;
    }

    public LocalDateTime getProgressCompletedAt() {
        return progressCompletedAt;
    }

    public void setProgressCompletedAt(LocalDateTime progressCompletedAt) {
        this.progressCompletedAt = progressCompletedAt;
    }

    public LocalDateTime getProgressLastAccessedAt() {
        return progressLastAccessedAt;
    }

    public void setProgressLastAccessedAt(LocalDateTime progressLastAccessedAt) {
        this.progressLastAccessedAt = progressLastAccessedAt;
    }
}