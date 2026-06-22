//
//
//package com.lms.auth.model;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Entity
//@Table(name = "organizations")
//public class Organization {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(name = "id", updatable = false, nullable = false)
//    private UUID id;
//
//    // ── Super admin fields ──────────────────────────────────
//    @Column(name = "name", nullable = false)
//    private String name;
//
//    @Column(name = "email")
//    private String email;
//
//    @Column(name = "city")
//    private String city;
//
//    @Column(name = "phone")
//    private String phone;
//
//    @Column(name = "plan", length = 50)
//    private String plan;
//
//    @Column(name = "status", length = 30)
//    private String status;
//
//    @Column(name = "manager_name")
//    private String managerName;
//
//    @Column(name = "manager_email")
//    private String managerEmail;
//
//    @Column(name = "owner_id")
//    private Long ownerId;
//
//    @Column(name = "max_students")
//    private Integer maxStudents;
//
//    @Column(name = "max_trainers")
//    private Integer maxTrainers;
//
//    @Column(name = "plan_expiry_date")
//    private String planExpiryDate;
//
//    // ── Admin self-fill fields ──────────────────────────────
//    @Column(name = "organization_name")
//    private String organizationName;   // display name admin sets
//
//    @Column(name = "domain")
//    private String domain;
//
//    @Column(name = "contact_email")
//    private String contactEmail;
//
//    @Column(name = "location")
//    private String location;
//
//    @Column(name = "industry")
//    private String industry;
//
//    @Column(name = "description", columnDefinition = "TEXT")
//    private String description;
//
//    @Column(name = "mobile_number")
//    private String mobileNumber;
//
//    // ── Timestamps ──────────────────────────────────────────
//    @Column(name = "created_at", updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updated_at")
//    private LocalDateTime updatedAt;
//
//    @PrePersist
//    public void prePersist() {
//        this.createdAt = LocalDateTime.now();
//        this.updatedAt = LocalDateTime.now();
//        if (this.plan   == null) this.plan   = "trial";
//        if (this.status == null) this.status = "active";
//    }
//
//    @PreUpdate
//    public void preUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
//
//    // ── Getters & Setters ───────────────────────────────────
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
//    public Long getOwnerId() { return ownerId; }
//    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
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
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//}



// OPTIMIZATION: Added @Index on status (for public org dropdown query)
// and owner_id (for findByOwnerId lookup).

package com.lms.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "organizations",
    indexes = {
        @Index(name = "idx_org_status",   columnList = "status"),
        @Index(name = "idx_org_owner_id", columnList = "owner_id"),
        @Index(name = "idx_org_email",    columnList = "email")
    }
)
public class Organization {
    // All fields unchanged — only @Table annotation added above
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "city")
    private String city;

    @Column(name = "phone")
    private String phone;

    @Column(name = "plan", length = 50)
    private String plan;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "manager_email")
    private String managerEmail;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "max_trainers")
    private Integer maxTrainers;
    

@Column(name = "max_departments")
private Integer maxDepartments;

@Column(name = "max_branches_per_dept")
private Integer maxBranchesPerDept;

@Column(name = "max_batches_per_branch")
private Integer maxBatchesPerBranch;

    @Column(name = "plan_expiry_date")
    private String planExpiryDate;

    @Column(name = "organization_name")
    private String organizationName;

    @Column(name = "domain")
    private String domain;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "location")
    private String location;

    @Column(name = "industry")
    private String industry;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    
 

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.plan   == null) this.plan   = "trial";
        if (this.status == null) this.status = "active";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    

    // All existing getters/setters unchanged
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
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public Integer getMaxStudents() { return maxStudents; }
    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
    public Integer getMaxTrainers() { return maxTrainers; }
    public void setMaxTrainers(Integer maxTrainers) { this.maxTrainers = maxTrainers; }
    public String getPlanExpiryDate() { return planExpiryDate; }
    public void setPlanExpiryDate(String planExpiryDate) { this.planExpiryDate = planExpiryDate; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
 // Getters & Setters
    public Integer getMaxDepartments()           { return maxDepartments; }
    public void setMaxDepartments(Integer v)     { this.maxDepartments = v; }

    public Integer getMaxBranchesPerDept()       { return maxBranchesPerDept; }
    public void setMaxBranchesPerDept(Integer v) { this.maxBranchesPerDept = v; }

    public Integer getMaxBatchesPerBranch()       { return maxBatchesPerBranch; }
    public void setMaxBatchesPerBranch(Integer v) { this.maxBatchesPerBranch = v; }
}