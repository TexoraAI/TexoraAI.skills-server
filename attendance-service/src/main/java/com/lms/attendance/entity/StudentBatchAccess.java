package com.lms.attendance.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "student_batch_access",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"batchId", "studentEmail"}
    )
)
public class StudentBatchAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;

    private Long studentUserId;

    private String studentEmail;

    // ===== Constructors =====
    public StudentBatchAccess() {}

    public StudentBatchAccess(Long batchId, Long studentUserId, String studentEmail) {
        this.batchId = batchId;
        this.studentUserId = studentUserId;
        this.studentEmail = studentEmail;
    }

    // ===== Getters & Setters =====

    public Long getId() {
        return id;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(Long studentUserId) {
        this.studentUserId = studentUserId;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
}
