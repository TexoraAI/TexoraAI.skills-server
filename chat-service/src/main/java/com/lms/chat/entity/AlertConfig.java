package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_config")
public class AlertConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false, unique = true)
    private Long batchId;

    @Column(name = "trainer_email", nullable = false)
    private String trainerEmail;

    @Column(name = "send_to_trainer")
    private boolean sendToTrainer = true;

    @Column(name = "send_to_student")
    private boolean sendToStudent = true;

    @Column(name = "send_to_admin")
    private boolean sendToAdmin = true;

    @Column(name = "alert_low_ratings")
    private boolean alertLowRatings = true;

    @Column(name = "low_rating_threshold")
    private Double lowRatingThreshold = 2.0;

    @Column(name = "alert_anonymous")
    private boolean alertAnonymous = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Column(name = "trainer_message", columnDefinition = "TEXT")
    private String trainerMessage;

    @Column(name = "student_message", columnDefinition = "TEXT")
    private String studentMessage;

    @Column(name = "admin_message", columnDefinition = "TEXT")
    private String adminMessage;

    // Getters & Setters
    public String getTrainerMessage() { return trainerMessage; }
    public void setTrainerMessage(String trainerMessage) { this.trainerMessage = trainerMessage; }

    public String getStudentMessage() { return studentMessage; }
    public void setStudentMessage(String studentMessage) { this.studentMessage = studentMessage; }

    public String getAdminMessage() { return adminMessage; }
    public void setAdminMessage(String adminMessage) { this.adminMessage = adminMessage; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public boolean isSendToTrainer() { return sendToTrainer; }
    public void setSendToTrainer(boolean sendToTrainer) { this.sendToTrainer = sendToTrainer; }

    public boolean isSendToStudent() { return sendToStudent; }
    public void setSendToStudent(boolean sendToStudent) { this.sendToStudent = sendToStudent; }

    public boolean isSendToAdmin() { return sendToAdmin; }
    public void setSendToAdmin(boolean sendToAdmin) { this.sendToAdmin = sendToAdmin; }

    public boolean isAlertLowRatings() { return alertLowRatings; }
    public void setAlertLowRatings(boolean alertLowRatings) { this.alertLowRatings = alertLowRatings; }

    public Double getLowRatingThreshold() { return lowRatingThreshold; }
    public void setLowRatingThreshold(Double lowRatingThreshold) { this.lowRatingThreshold = lowRatingThreshold; }

    public boolean isAlertAnonymous() { return alertAnonymous; }
    public void setAlertAnonymous(boolean alertAnonymous) { this.alertAnonymous = alertAnonymous; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}