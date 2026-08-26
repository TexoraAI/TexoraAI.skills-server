package com.lms.progress.exception;

/**
 * Thrown when a create/update to a node's parentNodeIds would introduce a cycle
 * into the template or org-roadmap node DAG. The proposed change is never
 * persisted and cycles are never auto-broken. Part 2B maps this to
 * 400 INVALID_NODE_GRAPH.
 */
public class RoadmapCycleException extends RuntimeException {

    public RoadmapCycleException(String message) {
        super(message);
    }
}