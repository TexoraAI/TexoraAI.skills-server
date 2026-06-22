//
//
//package com.lms.course.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "student_batch_map", indexes = {
//    @Index(name = "idx_sbm_student_email", columnList = "studentEmail"),
//    @Index(name = "idx_sbm_batch_id",      columnList = "batchId"),
//    @Index(name = "idx_sbm_org_id",        columnList = "organization_id"),
//    @Index(name = "idx_sbm_email_batch",   columnList = "studentEmail, batchId")
//})
//public class StudentBatchMap {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String studentEmail;
//
//    private Long batchId;
//
//    @Column(name = "organization_id")
//    private String organizationId; // NEW — added for multi-tenancy
//
//    public StudentBatchMap() {}
//
//    // Existing constructor — kept for backward compatibility (non-org users)
//    public StudentBatchMap(String studentEmail, Long batchId) {
//        this.studentEmail   = studentEmail;
//        this.batchId        = batchId;
//        this.organizationId = null;
//    }
//
//    // NEW constructor — used for org-based users
//    public StudentBatchMap(String studentEmail, Long batchId, String organizationId) {
//        this.studentEmail   = studentEmail;
//        this.batchId        = batchId;
//        this.organizationId = organizationId;
//    }
//
//    public Long getId()                            { return id; }
//    public void setId(Long id)                     { this.id = id; }
//
//    public String getStudentEmail()                { return studentEmail; }
//    public void setStudentEmail(String e)          { this.studentEmail = e; }
//
//    public Long getBatchId()                       { return batchId; }
//    public void setBatchId(Long b)                 { this.batchId = b; }
//
//    public String getOrganizationId()              { return organizationId; }
//    public void setOrganizationId(String orgId)    { this.organizationId = orgId; }
//}
package com.lms.course.model;

import jakarta.persistence.*;

// OPTIMIZATION: Added composite index on studentEmail+batchId.
// existsByStudentEmailAndBatchId is called on every student course-list request —
// this composite index makes it a single index scan instead of two.
@Entity
@Table(name = "student_batch_map", indexes = {
    @Index(name = "idx_sbm_student_email", columnList = "studentEmail"),
    @Index(name = "idx_sbm_batch_id",      columnList = "batchId"),
    @Index(name = "idx_sbm_org_id",        columnList = "organization_id"),
    @Index(name = "idx_sbm_email_batch",   columnList = "studentEmail, batchId")
})
public class StudentBatchMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentEmail;

    private Long batchId;

    @Column(name = "organization_id")
    private String organizationId;

    public StudentBatchMap() {}

    public StudentBatchMap(String studentEmail, Long batchId) {
        this.studentEmail   = studentEmail;
        this.batchId        = batchId;
        this.organizationId = null;
    }

    public StudentBatchMap(String studentEmail, Long batchId, String organizationId) {
        this.studentEmail   = studentEmail;
        this.batchId        = batchId;
        this.organizationId = organizationId;
    }

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public String getStudentEmail()                { return studentEmail; }
    public void setStudentEmail(String e)          { this.studentEmail = e; }

    public Long getBatchId()                       { return batchId; }
    public void setBatchId(Long b)                 { this.batchId = b; }

    public String getOrganizationId()              { return organizationId; }
    public void setOrganizationId(String orgId)    { this.organizationId = orgId; }
}