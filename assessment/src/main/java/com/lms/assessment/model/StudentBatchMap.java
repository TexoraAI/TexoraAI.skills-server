//package com.lms.assessment.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "student_batch_map")
//public class StudentBatchMap {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String studentEmail;
//    private Long batchId;
//
//    public StudentBatchMap() {}
//
//    public StudentBatchMap(String studentEmail, Long batchId) {
//        this.studentEmail = studentEmail;
//        this.batchId = batchId;
//    }
//
//    public String getStudentEmail() { return studentEmail; }
//    public Long getBatchId() { return batchId; }
//}
package com.lms.assessment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_batch_map")
public class StudentBatchMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentEmail;
    private Long batchId;

    // 🏢 Multi-tenancy — null for standalone users, expected.
    @Column(name = "organization_id")
    private String organizationId;

    public StudentBatchMap() {}

    public StudentBatchMap(String studentEmail, Long batchId) {
        this.studentEmail = studentEmail;
        this.batchId = batchId;
    }

    public StudentBatchMap(String studentEmail, Long batchId, String organizationId) {
        this.studentEmail = studentEmail;
        this.batchId = batchId;
        this.organizationId = organizationId;
    }

    public String getStudentEmail() { return studentEmail; }
    public Long getBatchId() { return batchId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}