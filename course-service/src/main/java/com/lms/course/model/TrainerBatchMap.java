package com.lms.course.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
@Entity
@Table(
    name = "trainer_batch_map",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"trainerEmail", "batchId"})
    }
)
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

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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