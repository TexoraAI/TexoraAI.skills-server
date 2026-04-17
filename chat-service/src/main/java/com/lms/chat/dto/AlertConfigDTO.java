package com.lms.chat.dto;

import com.lms.chat.entity.AlertConfig;
import java.time.LocalDateTime;

public class AlertConfigDTO {

    private Long id;
    private Long batchId;
    private String trainerEmail;
    private boolean sendToTrainer;
    private boolean sendToStudent;
    private boolean sendToAdmin;
    private boolean alertLowRatings;
    private Double lowRatingThreshold;
    private boolean alertAnonymous;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trainerMessage;
    private String studentMessage;
    private String adminMessage;


    public static AlertConfigDTO from(AlertConfig config) {
        AlertConfigDTO dto = new AlertConfigDTO();
        dto.id = config.getId();
        dto.batchId = config.getBatchId();
        dto.trainerEmail = config.getTrainerEmail();
        dto.sendToTrainer = config.isSendToTrainer();
        dto.sendToStudent = config.isSendToStudent();
        dto.sendToAdmin = config.isSendToAdmin();
        dto.alertLowRatings = config.isAlertLowRatings();
        dto.lowRatingThreshold = config.getLowRatingThreshold();
        dto.alertAnonymous = config.isAlertAnonymous();
        dto.createdAt = config.getCreatedAt();
        dto.updatedAt = config.getUpdatedAt();
        dto.setTrainerMessage(config.getTrainerMessage());   // ✅ ADD
        dto.setStudentMessage(config.getStudentMessage());   // ✅ ADD
        dto.setAdminMessage(config.getAdminMessage());       // ✅ ADD

        return dto;
    }

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
    public String getTrainerMessage() { return trainerMessage; }
    public void setTrainerMessage(String trainerMessage) { this.trainerMessage = trainerMessage; }

    public String getStudentMessage() { return studentMessage; }
    public void setStudentMessage(String studentMessage) { this.studentMessage = studentMessage; }

    public String getAdminMessage() { return adminMessage; }
    public void setAdminMessage(String adminMessage) { this.adminMessage = adminMessage; }

}