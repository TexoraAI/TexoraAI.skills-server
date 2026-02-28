package com.lms.attendance.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trainer_batch_access",
       uniqueConstraints = @UniqueConstraint(columnNames = {"batchId","trainerEmail"}))
public class TrainerBatchAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;

    private String trainerEmail;

    public TrainerBatchAccess() {}

    public TrainerBatchAccess(Long batchId, String trainerEmail) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
    }

    public Long getBatchId() { return batchId; }
    public String getTrainerEmail() { return trainerEmail; }
}
