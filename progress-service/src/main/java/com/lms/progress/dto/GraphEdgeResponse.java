package com.lms.progress.dto;

/**
 * A directed edge in the roadmap graph, from a parent (prerequisite)
 * node to a child (dependent) node.
 */
public class GraphEdgeResponse {

    private Long fromNodeId;
    private Long toNodeId;

    public GraphEdgeResponse() {
    }

    public GraphEdgeResponse(Long fromNodeId, Long toNodeId) {
        this.fromNodeId = fromNodeId;
        this.toNodeId = toNodeId;
    }

    public Long getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(Long fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public Long getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(Long toNodeId) {
        this.toNodeId = toNodeId;
    }
}
