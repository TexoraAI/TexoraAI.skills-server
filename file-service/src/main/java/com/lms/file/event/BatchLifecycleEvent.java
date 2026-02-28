package com.lms.file.event;

public class BatchLifecycleEvent {

    private String type;
    private Long batchId;
    private Long branchId;
    private String email;

    public BatchLifecycleEvent() {}

    public BatchLifecycleEvent(String type, Long batchId, Long branchId, String email) {
        this.type = type;
        this.batchId = batchId;
        this.branchId = branchId;
        this.email = email;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
