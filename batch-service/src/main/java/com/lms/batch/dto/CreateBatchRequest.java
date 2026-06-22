//package com.lms.batch.dto;
//
//public class CreateBatchRequest {
//
//    private String batchName;
//    private Long branchId;
//    private Long trainerId;
//    private String trainerEmail;
//
//    public String getBatchName() {
//        return batchName;
//    }
//
//    public String getTrainerEmail() {
//        return trainerEmail;
//    }
//
//    public void setTrainerEmail(String trainerEmail) {
//        this.trainerEmail = trainerEmail;
//    }
//    public void setBatchName(String batchName) {
//        this.batchName = batchName;
//    }
//
//    public Long getBranchId() {
//        return branchId;
//    }
//
//    public void setBranchId(Long branchId) {
//        this.branchId = branchId;
//    }
//
//    public Long getTrainerId() {
//        return trainerId;
//    }
//
//    public void setTrainerId(Long trainerId) {
//        this.trainerId = trainerId;
//    }
//}
package com.lms.batch.dto;

public class CreateBatchRequest {

    private String batchName;
    private Long   branchId;
    private Long   trainerId;     // keep — do not break existing callers
    private String trainerEmail;

    // NEW — resolved from branch at service layer; optionally passed by client
    private Long   departmentId;
    private String organizationId; // UUID from auth-service

    public String getBatchName()               { return batchName; }
    public void   setBatchName(String v)       { this.batchName = v; }

    public Long   getBranchId()                { return branchId; }
    public void   setBranchId(Long v)          { this.branchId = v; }

    public Long   getTrainerId()               { return trainerId; }
    public void   setTrainerId(Long v)         { this.trainerId = v; }

    public String getTrainerEmail()            { return trainerEmail; }
    public void   setTrainerEmail(String v)    { this.trainerEmail = v; }

    // NEW
    public Long   getDepartmentId()            { return departmentId; }
    public void   setDepartmentId(Long v)      { this.departmentId = v; }

    public String getOrganizationId()          { return organizationId; }
    public void   setOrganizationId(String v)  { this.organizationId = v; }
}