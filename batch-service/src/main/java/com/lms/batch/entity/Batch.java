//
//
//package com.lms.batch.entity;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "batches")
//public class Batch {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String batchCode;
//    private String batchName;
//
//    @Column(nullable = false)
//    private Long branchId;
//
//    // NEW — copied from branch.departmentId at creation time
//    @Column(name = "department_id")
//    private Long departmentId;
//
//    // NEW — copied from branch.organizationId at creation time
//    @Column(name = "organization_id")
//    private String organizationId; // UUID from auth-service, stored as String
//
//    // OPTIONAL KEEP — do not remove
//    private Long trainerId;
//
//    // MAIN FIELD
//    @Column(name = "trainer_email")
//    private String trainerEmail;
//
//    private boolean active = true;
//
//    // ===== Getters & Setters =====
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getBatchCode() { return batchCode; }
//    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }
//
//    public String getBatchName() { return batchName; }
//    public void setBatchName(String batchName) { this.batchName = batchName; }
//
//    public Long getBranchId() { return branchId; }
//    public void setBranchId(Long branchId) { this.branchId = branchId; }
//
//    public Long getDepartmentId() { return departmentId; }
//    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
//
//    public String getOrganizationId() { return organizationId; }
//    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
//
//    public Long getTrainerId() { return trainerId; }
//    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }
//
//    public String getTrainerEmail() { return trainerEmail; }
//    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }
//
//    public boolean isActive() { return active; }
//    public void setActive(boolean active) { this.active = active; }
//}

package com.lms.batch.entity;

import jakarta.persistence.*;

// OPTIMIZATION: Added @Index annotations on all queried columns.
// organization_id, branch_id, trainer_email were unindexed causing full table scans.
@Entity
@Table(name = "batches", indexes = {
    @Index(name = "idx_batches_org_id",       columnList = "organization_id"),
    @Index(name = "idx_batches_branch_id",    columnList = "branchId"),
    @Index(name = "idx_batches_trainer_email",columnList = "trainer_email"),
    @Index(name = "idx_batches_dept_id",      columnList = "department_id")
})
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String batchCode;
    private String batchName;

    @Column(nullable = false)
    private Long branchId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "organization_id")
    private String organizationId;

    private Long trainerId;

    @Column(name = "trainer_email")
    private String trainerEmail;

    private boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchCode() { return batchCode; }
    public void setBatchCode(String batchCode) { this.batchCode = batchCode; }

    public String getBatchName() { return batchName; }
    public void setBatchName(String batchName) { this.batchName = batchName; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}