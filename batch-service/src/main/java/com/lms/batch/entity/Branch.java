//
//
//package com.lms.batch.entity;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "branches")
//public class Branch {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;
//    private String city;
//
//    // NEW — links branch to its department
//    @Column(name = "department_id")
//    private Long departmentId;
//
//    // NEW — copied from department.organizationId at creation time
//    @Column(name = "organization_id")
//    private String organizationId; // UUID from auth-service, stored as String
//
//    // ===== Getters & Setters =====
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getCity() { return city; }
//    public void setCity(String city) { this.city = city; }
//
//    public Long getDepartmentId() { return departmentId; }
//    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
//
//    public String getOrganizationId() { return organizationId; }
//    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
//}

package com.lms.batch.entity;

import jakarta.persistence.*;

// OPTIMIZATION: Added indexes on department_id and organization_id.
// findByDepartmentId (cascade delete) and findByOrganizationId were full table scans.
@Entity
@Table(name = "branches", indexes = {
    @Index(name = "idx_branches_dept_id", columnList = "department_id"),
    @Index(name = "idx_branches_org_id",  columnList = "organization_id")
})
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "organization_id")
    private String organizationId;

    public Long getId()                                  { return id; }
    public void setId(Long id)                           { this.id = id; }
    public String getName()                              { return name; }
    public void setName(String name)                     { this.name = name; }
    public String getCity()                              { return city; }
    public void setCity(String city)                     { this.city = city; }
    public Long getDepartmentId()                        { return departmentId; }
    public void setDepartmentId(Long departmentId)       { this.departmentId = departmentId; }
    public String getOrganizationId()                    { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}