//package com.lms.auth.dto;
//
//public class CreateOrganizationRequest {
//
//    private String name;
//    private String email;
//    private String city;
//    private String phone;
//    private String plan;
//    private String status;
//    private String managerName;
//    private String managerEmail;
//
//    public CreateOrganizationRequest() {}
//    private Integer maxStudents;
//    private Integer maxTrainers;
//
//    public Integer getMaxStudents() { return maxStudents; }
//    public void setMaxStudents(Integer maxStudents) { this.maxStudents = maxStudents; }
//    public Integer getMaxTrainers() { return maxTrainers; }
//    public void setMaxTrainers(Integer maxTrainers) { this.maxTrainers = maxTrainers; }
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
//}

package com.lms.auth.dto;

public class CreateOrganizationRequest {

    // ── Super admin fields ──
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
    private String planExpiryDate;   // ← NEW
 // Organization.java
   
    public CreateOrganizationRequest() {}

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
    public Integer getMaxDepartments()           { return maxDepartments; }
    public void setMaxDepartments(Integer v)     { this.maxDepartments = v; }

    public Integer getMaxBranchesPerDept()       { return maxBranchesPerDept; }
    public void setMaxBranchesPerDept(Integer v) { this.maxBranchesPerDept = v; }

    public Integer getMaxBatchesPerBranch()       { return maxBatchesPerBranch; }
    public void setMaxBatchesPerBranch(Integer v) { this.maxBatchesPerBranch = v; }
}