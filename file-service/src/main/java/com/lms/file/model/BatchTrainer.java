package com.lms.file.model;

import jakarta.persistence.*;

@Entity
@Table(name = "batch_trainer_map")
public class BatchTrainer {

    @Id
    private Long batchId;

    private String trainerEmail;

    public BatchTrainer() {}

    public BatchTrainer(Long batchId, String trainerEmail) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }
}
