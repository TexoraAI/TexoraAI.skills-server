package com.lms.assessment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_trainer_map")
public class StudentTrainerMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_email", nullable = false)
    private String studentEmail;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    public StudentTrainerMap() {}

    public StudentTrainerMap(String studentEmail, String trainerEmail, Long batchId,boolean active) {
        this.studentEmail = studentEmail;
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
        this.active=active;
    }
    @Column(nullable = false)
    private boolean active = true;

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Long getId() { return id; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
}