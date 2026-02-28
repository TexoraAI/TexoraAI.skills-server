package com.lms.batch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "batch_trainer_student")
public class BatchTrainerStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private String trainerEmail;

    @Column(nullable = false)
    private String studentEmail;

    // ===== Constructors =====

    public BatchTrainerStudent() {
    }

    public BatchTrainerStudent(Long batchId, String trainerEmail, String studentEmail) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
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

    public String getTrainerEmail() {
        return trainerEmail;
    }

    public void setTrainerEmail(String trainerEmail) {
        this.trainerEmail = trainerEmail;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
}
