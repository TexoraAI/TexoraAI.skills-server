package com.lms.attendance.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "trainer_session_attendance",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "batch_id",
                "trainer_email",
                "session_date"
            }
        )
    }
)
public class TrainerSessionAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    // nullable — null for standalone/orgless trainers, matches Attendance.java pattern
    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;

    @PrePersist
    protected void onCreate() {
        this.markedAt = LocalDateTime.now();
    }

    public TrainerSessionAttendance() {}

    // ========= GETTERS & SETTERS =========

    public Long getId() { return id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public LocalDateTime getMarkedAt() { return markedAt; }
}