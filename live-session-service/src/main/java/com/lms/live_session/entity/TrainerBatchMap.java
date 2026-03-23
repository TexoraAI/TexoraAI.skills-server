package com.lms.live_session.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trainer_batch_map")
public class TrainerBatchMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trainerEmail;
    private Long batchId;

    public TrainerBatchMap() {}

    public TrainerBatchMap(String trainerEmail, Long batchId) {
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
    }

    public Long getId() {
        return id;
    }

    public String getTrainerEmail() {
        return trainerEmail;
    }

    public void setTrainerEmail(String trainerEmail) {
        this.trainerEmail = trainerEmail;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
}