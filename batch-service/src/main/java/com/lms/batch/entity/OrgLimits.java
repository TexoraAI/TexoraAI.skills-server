

package com.lms.batch.entity;

import jakarta.persistence.*;

// OPTIMIZATION: No index needed — organizationId IS the @Id (primary key, auto-indexed).
@Entity
@Table(name = "orglimits")
public class OrgLimits {

    @Id
    private String organizationId;
    private Integer maxDepartments;
    private Integer maxBranchesPerDept;
    private Integer maxBatchesPerBranch;

    public String getOrganizationId()                          { return organizationId; }
    public void setOrganizationId(String organizationId)       { this.organizationId = organizationId; }
    public Integer getMaxDepartments()                         { return maxDepartments; }
    public void setMaxDepartments(Integer maxDepartments)      { this.maxDepartments = maxDepartments; }
    public Integer getMaxBranchesPerDept()                     { return maxBranchesPerDept; }
    public void setMaxBranchesPerDept(Integer maxBranchesPerDept) { this.maxBranchesPerDept = maxBranchesPerDept; }
    public Integer getMaxBatchesPerBranch()                    { return maxBatchesPerBranch; }
    public void setMaxBatchesPerBranch(Integer maxBatchesPerBranch) { this.maxBatchesPerBranch = maxBatchesPerBranch; }
}