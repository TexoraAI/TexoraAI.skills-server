package com.lms.progress.dto;

import com.lms.progress.model.NodeType;

import java.util.List;

public class UpdateOrgNodeRequest {

    private String title;
    private String description;
    private NodeType type;
    private Double positionX;
    private Double positionY;
    private Boolean isOptional;
    private Integer estimatedHours;
    private Integer orderIndex;
    private Boolean hasQuiz;
    private Boolean hasProject;
    private List<Long> parentNodeIds;

    public UpdateOrgNodeRequest() {
    }

    public UpdateOrgNodeRequest(String title, String description, NodeType type, Double positionX,
                                 Double positionY, Boolean isOptional, Integer estimatedHours,
                                 Integer orderIndex, Boolean hasQuiz, Boolean hasProject,
                                 List<Long> parentNodeIds) {
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

    public Boolean getIsOptional() {
        return isOptional;
    }

    public void setIsOptional(Boolean isOptional) {
        this.isOptional = isOptional;
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

    public Boolean getHasQuiz() {
        return hasQuiz;
    }

    public void setHasQuiz(Boolean hasQuiz) {
        this.hasQuiz = hasQuiz;
    }

    public Boolean getHasProject() {
        return hasProject;
    }

    public void setHasProject(Boolean hasProject) {
        this.hasProject = hasProject;
    }

    public List<Long> getParentNodeIds() {
        return parentNodeIds;
    }

    public void setParentNodeIds(List<Long> parentNodeIds) {
        this.parentNodeIds = parentNodeIds;
    }
}
