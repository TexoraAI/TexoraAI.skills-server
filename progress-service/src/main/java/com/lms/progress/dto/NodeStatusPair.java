package com.lms.progress.dto;

import com.lms.progress.model.NodeStatus;

/**
 * A single { nodeId, status } pair used inside BatchUpdateProgressRequest.
 */
public class NodeStatusPair {

    private Long nodeId;
    private NodeStatus status;

    public NodeStatusPair() {
    }

    public NodeStatusPair(Long nodeId, NodeStatus status) {
        this.nodeId = nodeId;
        this.status = status;
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
}
