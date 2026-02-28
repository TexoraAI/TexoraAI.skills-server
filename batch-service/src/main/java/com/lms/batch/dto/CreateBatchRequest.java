package com.lms.batch.dto;

public class CreateBatchRequest {

    private String batchName;
    private Long branchId;
    private Long trainerId;
    private String trainerEmail;

    public String getBatchName() {
        return batchName;
    }

    public String getTrainerEmail() {
        return trainerEmail;
    }

    public void setTrainerEmail(String trainerEmail) {
        this.trainerEmail = trainerEmail;
    }
    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }
}
