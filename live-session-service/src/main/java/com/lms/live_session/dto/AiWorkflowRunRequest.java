package com.lms.live_session.dto;

public class AiWorkflowRunRequest {

    // Optional — which live session this manual run is scoped to.
    // Needed by node handlers that build AI context (wired in Phase 4b)
    // and gets stamped onto the resulting AiWorkflowRun row.
    private Long sessionId;

    public AiWorkflowRunRequest() {}

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
}