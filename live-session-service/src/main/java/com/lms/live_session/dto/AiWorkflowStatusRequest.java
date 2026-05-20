package com.lms.live_session.dto;

public class AiWorkflowStatusRequest {

    private String status;

    public AiWorkflowStatusRequest() {}

    public AiWorkflowStatusRequest(String status) {
        this.status = status;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}