package com.lms.live_session.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "student_batch_map")
public class StudentBatchMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentEmail;
    private Long batchId;

    @Column(name = "organization_id") // ✅ NEW — nullable by default, backward-compatible
    private Long organizationId;

    public StudentBatchMap() {}

    public StudentBatchMap(String studentEmail, Long batchId) {
        this.studentEmail = studentEmail;
        this.batchId = batchId;
    }

    // ✅ NEW overload
    public StudentBatchMap(String studentEmail, Long batchId, Long organizationId) {
        this.studentEmail = studentEmail;
        this.batchId = batchId;
        this.organizationId = organizationId;
    }

    public Long getId() {
        return id;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    // ✅ NEW
    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }
}