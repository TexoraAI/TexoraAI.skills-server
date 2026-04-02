package com.lms.notification.model;
import jakarta.persistence.*;

@Entity
@Table(name = "trainer_batch_map")
public class TrainerBatchMap {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trainerEmail;
    private Long batchId;

    public TrainerBatchMap() {}
    public TrainerBatchMap(String trainerEmail, Long batchId) {
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String e) { this.trainerEmail = e; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long b) { this.batchId = b; }
}