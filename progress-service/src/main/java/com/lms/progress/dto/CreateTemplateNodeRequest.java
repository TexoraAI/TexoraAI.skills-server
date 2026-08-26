package com.lms.progress.dto;

import com.lms.progress.model.NodeType;

import java.util.List;

public class CreateTemplateNodeRequest {

    private Long templateId;
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

    public CreateTemplateNodeRequest() {
    }

    public CreateTemplateNodeRequest(Long templateId, String title, String description, NodeType type,
                                      Double positionX, Double positionY, boolean isOptional,
                                      Integer estimatedHours, Integer orderIndex, boolean hasQuiz,
                                      boolean hasProject, List<Long> parentNodeIds) {
        this.templateId = templateId;
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

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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
}
