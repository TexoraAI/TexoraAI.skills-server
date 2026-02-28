package com.lms.assessment.model;

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

    public String getTrainerEmail() { return trainerEmail; }
    public Long getBatchId() { return batchId; }
}
