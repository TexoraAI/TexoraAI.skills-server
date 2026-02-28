package com.lms.batch.dto;

public class StudentBatchInfoDTO {

    private Long batchId;
    private String batchName;
    private String batchCode;

    private Long trainerId;
    private String trainerName;
    private String trainerEmail;

    // Default Constructor
    public StudentBatchInfoDTO() {
    }

    // All-args Constructor
    public StudentBatchInfoDTO(Long batchId, String batchName, String batchCode, 
                               Long trainerId, String trainerName, String trainerEmail) {
        this.batchId = batchId;
        this.batchName = batchName;
        this.batchCode = batchCode;
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        this.trainerEmail = trainerEmail;
    }

    // --- Getters and Setters ---

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getTrainerEmail() {
        return trainerEmail;
    }

    public void setTrainerEmail(String trainerEmail) {
        this.trainerEmail = trainerEmail;
    }
}