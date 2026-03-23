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

    public StudentBatchMap() {}

    public StudentBatchMap(String studentEmail, Long batchId) {
        this.studentEmail = studentEmail;
        this.batchId = batchId;
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
}