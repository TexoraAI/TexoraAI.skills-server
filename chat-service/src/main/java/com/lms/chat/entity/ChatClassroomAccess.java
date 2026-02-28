package com.lms.chat.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "chat_classroom_access",
       uniqueConstraints = @UniqueConstraint(columnNames = {
           "batchId","trainerEmail","studentEmail"
       }))
public class ChatClassroomAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;
    private String trainerEmail;
    private String studentEmail;

    public ChatClassroomAccess() {}

    public ChatClassroomAccess(Long batchId, String trainerEmail, String studentEmail) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
        this.studentEmail = studentEmail;
    }

    public Long getBatchId() { return batchId; }
    public String getTrainerEmail() { return trainerEmail; }
    public String getStudentEmail() { return studentEmail; }
}
