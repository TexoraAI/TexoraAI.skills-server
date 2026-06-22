//package com.lms.batch.entity;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "departments")
//public class Department {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column(name = "organization_id")
//    private String organizationId; // UUID from auth-service, stored as String
//
//    
//    @Column(name = "head")
//    private String head;
//
//    public String getHead() { return head; }
//    public void setHead(String head) { this.head = head; }
//    // ===== Getters & Setters =====
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getOrganizationId() { return organizationId; }
//    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
//}

package com.lms.batch.entity;

import jakarta.persistence.*;

// OPTIMIZATION: Added index on organization_id.
// findByOrganizationId and countByOrganizationId were full table scans.
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_departments_org_id", columnList = "organization_id")
})
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "head")
    private String head;

    public Long getId()                                  { return id; }
    public void setId(Long id)                           { this.id = id; }
    public String getName()                              { return name; }
    public void setName(String name)                     { this.name = name; }
    public String getOrganizationId()                    { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public String getHead()                              { return head; }
    public void setHead(String head)                     { this.head = head; }
}