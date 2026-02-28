package com.lms.batch.event;

public class BatchAssignmentEvent {

    private String type;
    private String email;
    private Long batchId;
    private String role; // STUDENT or TRAINER

    public BatchAssignmentEvent() {}

    public BatchAssignmentEvent(String type, String email, Long batchId, String role) {
        this.type = type;
        this.email = email;
        this.batchId = batchId;
        this.role = role;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
