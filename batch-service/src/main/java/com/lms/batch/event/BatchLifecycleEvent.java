//package com.lms.batch.event;
//
//public class BatchLifecycleEvent {
//
//    private String type;
//    private Long batchId;
//    private Long branchId;
//    private String email;
//
//    public BatchLifecycleEvent() {}
//
//    public BatchLifecycleEvent(String type, Long batchId, Long branchId, String email) {
//        this.type = type;
//        this.batchId = batchId;
//        this.branchId = branchId;
//        this.email = email;
//    }
//
//    public String getType() { return type; }
//    public void setType(String type) { this.type = type; }
//
//    public Long getBatchId() { return batchId; }
//    public void setBatchId(Long batchId) { this.batchId = batchId; }
//
//    public Long getBranchId() { return branchId; }
//    public void setBranchId(Long branchId) { this.branchId = branchId; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//}
package com.lms.batch.event;

public class BatchLifecycleEvent {

    private String type;
    private Long   batchId;
    private Long   branchId;
    private Long   departmentId; // NEW
    private String email;

    public BatchLifecycleEvent() {}

    // NEW — updated constructor with departmentId
    public BatchLifecycleEvent(String type, Long batchId, Long branchId,
                               Long departmentId, String email) {
        this.type         = type;
        this.batchId      = batchId;
        this.branchId     = branchId;
        this.departmentId = departmentId;
        this.email        = email;
    }

    public String getType()           { return type; }
    public void   setType(String v)   { this.type = v; }

    public Long   getBatchId()        { return batchId; }
    public void   setBatchId(Long v)  { this.batchId = v; }

    public Long   getBranchId()       { return branchId; }
    public void   setBranchId(Long v) { this.branchId = v; }

    // NEW
    public Long   getDepartmentId()        { return departmentId; }
    public void   setDepartmentId(Long v)  { this.departmentId = v; }

    public String getEmail()          { return email; }
    public void   setEmail(String v)  { this.email = v; }
}