//package com.lms.video.model;
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
//    private String organizationId;
//    public StudentBatchMap() {}
//
//    public StudentBatchMap(String studentEmail, Long batchId) {
//        this.studentEmail = studentEmail;
//        this.batchId = batchId;
//    }
//    public StudentBatchMap(String studentEmail, Long batchId,String organizationId) {
//        this.studentEmail = studentEmail;
//        this.batchId = batchId;
//        this.organizationId = organizationId;
//    }
//
//    public String getStudentEmail() { return studentEmail; }
//    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
//
//    public Long getBatchId() { return batchId; }
//    public void setBatchId(Long batchId) { this.batchId = batchId; }
//    
//
//public String getOrganizationId() { return organizationId; }
//public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
//
//}
package com.lms.video.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_batch_map", indexes = {
        @Index(name = "idx_student_map_org", columnList = "organization_id")
})
public class StudentBatchMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentEmail;
    private Long batchId;

    // ✅ NEW — carried over from the batch-assignment Kafka event.
    private String organizationId;

    public StudentBatchMap() {}

    public StudentBatchMap(String studentEmail, Long batchId) {
        this.studentEmail = studentEmail;
        this.batchId = batchId;
    }

    // ✅ NEW — overload used once organizationId is available from the event
    public StudentBatchMap(String studentEmail, Long batchId, String organizationId) {
        this.studentEmail = studentEmail;
        this.batchId = batchId;
        this.organizationId = organizationId;
    }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    // ✅ NEW
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}