package com.lms.live_session.event;

public class BatchAssignmentEvent {

    private String type;
    private String email;
    private Long batchId;
    private String role;
    private Long organizationId; // ✅ NEW — nullable, non-org users/events will have null

    public BatchAssignmentEvent() {}

    public BatchAssignmentEvent(String type, String email, Long batchId, String role) {
        this.type = type;
        this.email = email;
        this.batchId = batchId;
        this.role = role;
    }

    // ✅ NEW overload — used once producer payload is confirmed to include organizationId
    public BatchAssignmentEvent(String type, String email, Long batchId, String role, Long organizationId) {
        this.type = type;
        this.email = email;
        this.batchId = batchId;
        this.role = role;
        this.organizationId = organizationId;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // ✅ NEW
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}