
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

    // Nullable: null for non-organization (standalone) users, set for org-based users
    private String organizationId;

    public ChatClassroomAccess() {}

    public ChatClassroomAccess(Long batchId, String trainerEmail, String studentEmail) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
        this.studentEmail = studentEmail;
    }

    public ChatClassroomAccess(Long batchId, String trainerEmail, String studentEmail, String organizationId) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
        this.studentEmail = studentEmail;
        this.organizationId = organizationId;
    }

    public Long getBatchId() { return batchId; }
    public String getTrainerEmail() { return trainerEmail; }
    public String getStudentEmail() { return studentEmail; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}