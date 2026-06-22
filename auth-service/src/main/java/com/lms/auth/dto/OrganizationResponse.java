//
//
//
//package com.lms.auth.dto;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//public class OrganizationResponse {
//
//    // ── Super admin fields ──
//    private UUID id;
//    private String name;
//    private String email;
//    private String city;
//    private String phone;
//    private String plan;
//    private String status;
//    private String managerName;
//    private String managerEmail;
//    private Integer maxStudents;
//    private Integer maxTrainers;
//    private String planExpiryDate;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//
//    // ── Admin self-fill fields ──
//    private String organizationName;
//    private String domain;
//    private String contactEmail;
//    private String location;
//    private String industry;
//    private String description;
//    private String mobileNumber;
//
//    // ── Live counts (from capacity) ──
//    private Long currentStudents;
//    private Long currentTrainers;
//
//    public OrganizationResponse() {}
//
//    public UUID getId() { return id; }
//    public void setId(UUID id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getCity() { return city; }
//    public void setCity(String city) { this.city = city; }
//
//    public String getPhone() { return phone; }
//    public void setPhone(String phone) { this.phone = phone; }
//
//    public String getPlan() { return plan; }
//    public void setPlan(String plan) { this.plan = plan; }
//
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//
//    public String getManagerName() { return managerName; }
//    public void setManagerName(String managerName) { this.managerName = managerName; }
//
//    public String getManagerEmail() { return managerEmail; }
//    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }
//
//    public Integer getMaxStudents() { return maxStudents; }
//    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
//
//    public Integer getMaxTrainers() { return maxTrainers; }
//    public void setMaxTrainers(Integer maxTrainers) { this.maxTrainers = maxTrainers; }
//
//    public String getPlanExpiryDate() { return planExpiryDate; }
//    public void setPlanExpiryDate(String planExpiryDate) { this.planExpiryDate = planExpiryDate; }
//
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//
//    public String getOrganizationName() { return organizationName; }
//    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
//
//    public String getDomain() { return domain; }
//    public void setDomain(String domain) { this.domain = domain; }
//
//    public String getContactEmail() { return contactEmail; }
//    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
//
//    public String getLocation() { return location; }
//    public void setLocation(String location) { this.location = location; }
//
//    public String getIndustry() { return industry; }
//    public void setIndustry(String industry) { this.industry = industry; }
//
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//
//    public String getMobileNumber() { return mobileNumber; }
//    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
//
//    public Long getCurrentStudents() { return currentStudents; }
//    public void setCurrentStudents(Long currentStudents) { this.currentStudents = currentStudents; }
//
//    public Long getCurrentTrainers() { return currentTrainers; }
//    public void setCurrentTrainers(Long currentTrainers) { this.currentTrainers = currentTrainers; }
//}


package com.lms.auth.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

// OPTIMIZATION: Implemented Serializable so Redis can properly
// serialize and deserialize this object.
// Added @JsonFormat for LocalDateTime fields so Jackson handles
// them correctly when reading back from Redis cache.
public class OrganizationResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String email;
    private String city;
    private String phone;
    private String plan;
    private String status;
    private String managerName;
    private String managerEmail;
    private Integer maxStudents;
    private Integer maxTrainers;
    private Integer maxDepartments;
    private Integer maxBranchesPerDept;
    private Integer maxBatchesPerBranch;
    private String planExpiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String organizationName;
    private String domain;
    private String contactEmail;
    private String location;
    private String industry;
    private String description;
    private String mobileNumber;
    private Long currentStudents;
    private Long currentTrainers;
 // Organization.java
    

    public OrganizationResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getManagerEmail() { return managerEmail; }
    public void setManagerEmail(String managerEmail) { this.managerEmail = managerEmail; }
    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public Integer getMaxTrainers() { return maxTrainers; }
    public void setMaxTrainers(Integer maxTrainers) { this.maxTrainers = maxTrainers; }
    public String getPlanExpiryDate() { return planExpiryDate; }
    public void setPlanExpiryDate(String planExpiryDate) { this.planExpiryDate = planExpiryDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public Long getCurrentStudents() { return currentStudents; }
    public void setCurrentStudents(Long currentStudents) { this.currentStudents = currentStudents; }
    public Long getCurrentTrainers() { return currentTrainers; }
    public void setCurrentTrainers(Long currentTrainers) { this.currentTrainers = currentTrainers; }
    public Integer getMaxDepartments()           { return maxDepartments; }
    public void setMaxDepartments(Integer v)     { this.maxDepartments = v; }

    public Integer getMaxBranchesPerDept()       { return maxBranchesPerDept; }
    public void setMaxBranchesPerDept(Integer v) { this.maxBranchesPerDept = v; }

    public Integer getMaxBatchesPerBranch()       { return maxBatchesPerBranch; }
    public void setMaxBatchesPerBranch(Integer v) { this.maxBatchesPerBranch = v; }
}