//
//package com.lms.batch.dto;
//
//public class BatchResponseDTO {
//
//    private Long   id;
//    private String batchCode;
//    private String batchName;
//    private Long   branchId;
//    private Long   trainerId;     // keep — do not break existing frontend
//    private String trainerEmail;
//    private boolean active;
//
//    // NEW
//    private Long   departmentId;
//    private String organizationId; // UUID from auth-service
//
//    public Long   getId()                      { return id; }
//    public void   setId(Long v)                { this.id = v; }
//
//    public String getBatchCode()               { return batchCode; }
//    public void   setBatchCode(String v)       { this.batchCode = v; }
//
//    public String getBatchName()               { return batchName; }
//    public void   setBatchName(String v)       { this.batchName = v; }
//
//    public Long   getBranchId()                { return branchId; }
//    public void   setBranchId(Long v)          { this.branchId = v; }
//
//    public Long   getTrainerId()               { return trainerId; }
//    public void   setTrainerId(Long v)         { this.trainerId = v; }
//
//    public String getTrainerEmail()            { return trainerEmail; }
//    public void   setTrainerEmail(String v)    { this.trainerEmail = v; }
//
//    public boolean isActive()                  { return active; }
//    public void    setActive(boolean v)        { this.active = v; }
//
//    // NEW
//    public Long   getDepartmentId()            { return departmentId; }
//    public void   setDepartmentId(Long v)      { this.departmentId = v; }
//
//    public String getOrganizationId()          { return organizationId; }
//    public void   setOrganizationId(String v)  { this.organizationId = v; }
//}
package com.lms.batch.dto;

import java.io.Serializable;

// OPTIMIZATION: Implements Serializable required for Redis caching.
// Without this, GenericJackson2JsonRedisSerializer cannot serialize this DTO.
public class BatchResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long   id;
    private String batchCode;
    private String batchName;
    private Long   branchId;
    private Long   trainerId;
    private String trainerEmail;
    private boolean active;
    private Long   departmentId;
    private String organizationId;

    public BatchResponseDTO() {}

    public Long   getId()                      { return id; }
    public void   setId(Long v)                { this.id = v; }
    public String getBatchCode()               { return batchCode; }
    public void   setBatchCode(String v)       { this.batchCode = v; }
    public String getBatchName()               { return batchName; }
    public void   setBatchName(String v)       { this.batchName = v; }
    public Long   getBranchId()                { return branchId; }
    public void   setBranchId(Long v)          { this.branchId = v; }
    public Long   getTrainerId()               { return trainerId; }
    public void   setTrainerId(Long v)         { this.trainerId = v; }
    public String getTrainerEmail()            { return trainerEmail; }
    public void   setTrainerEmail(String v)    { this.trainerEmail = v; }
    public boolean isActive()                  { return active; }
    public void    setActive(boolean v)        { this.active = v; }
    public Long   getDepartmentId()            { return departmentId; }
    public void   setDepartmentId(Long v)      { this.departmentId = v; }
    public String getOrganizationId()          { return organizationId; }
    public void   setOrganizationId(String v)  { this.organizationId = v; }
}