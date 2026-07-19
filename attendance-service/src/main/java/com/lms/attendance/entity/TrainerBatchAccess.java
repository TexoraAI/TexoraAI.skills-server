
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

    // NEW — nullable, populated from trusted JWT/Kafka organizationId. Null for standalone trainers.
    private String organizationId;

    public TrainerBatchAccess() {}

    public TrainerBatchAccess(Long batchId, String trainerEmail) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
    }

    // NEW — overload used when organizationId is known (org-based trainers)
    public TrainerBatchAccess(Long batchId, String trainerEmail, String organizationId) {
        this(batchId, trainerEmail);
        this.organizationId = organizationId;
    }

    public Long getBatchId() { return batchId; }
    public String getTrainerEmail() { return trainerEmail; }

    // NEW
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}