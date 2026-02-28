package com.lms.file.model;

import jakarta.persistence.*;

@Entity
@Table(name = "file_classroom_access",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"batchId", "studentEmail"}
       ))
public class FileClassroomAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;

    private String trainerEmail;

    private String studentEmail;

    public FileClassroomAccess() {}

    public FileClassroomAccess(Long batchId, String trainerEmail, String studentEmail) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
        this.studentEmail = studentEmail;
    }

    public Long getId() { return id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
}
