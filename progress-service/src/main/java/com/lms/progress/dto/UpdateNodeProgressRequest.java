package com.lms.progress.dto;

import com.lms.progress.model.NodeStatus;

public class UpdateNodeProgressRequest {

    private Long nodeId;
    private NodeStatus status;
    private Integer additionalTimeSpentMinutes;
    private Boolean incrementResourceClick;

    public UpdateNodeProgressRequest() {
    }

    public UpdateNodeProgressRequest(Long nodeId, NodeStatus status, Integer additionalTimeSpentMinutes,
                                      Boolean incrementResourceClick) {
        this.nodeId = nodeId;
        this.status = status;
        this.additionalTimeSpentMinutes = additionalTimeSpentMinutes;
        this.incrementResourceClick = incrementResourceClick;
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

    public Integer getAdditionalTimeSpentMinutes() {
        return additionalTimeSpentMinutes;
    }

    public void setAdditionalTimeSpentMinutes(Integer additionalTimeSpentMinutes) {
        this.additionalTimeSpentMinutes = additionalTimeSpentMinutes;
    }

    public Boolean getIncrementResourceClick() {
        return incrementResourceClick;
    }

    public void setIncrementResourceClick(Boolean incrementResourceClick) {
        this.incrementResourceClick = incrementResourceClick;
    }
}
