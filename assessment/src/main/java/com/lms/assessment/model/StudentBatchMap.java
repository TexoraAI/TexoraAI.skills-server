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

    public StudentBatchMap() {}

    public StudentBatchMap(String studentEmail, Long batchId) {
        this.studentEmail = studentEmail;
        this.batchId = batchId;
    }

    public String getStudentEmail() { return studentEmail; }
    public Long getBatchId() { return batchId; }
}
