
package com.lms.course.model;

import jakarta.persistence.*;

// OPTIMIZATION: Added composite indexes:
//   trainerEmail+batchId       — existsByTrainerEmailAndBatchId (called on every course create)
//   trainerEmail+batchId+orgId — existsByTrainerEmailAndBatchIdAndOrganizationId (org validation)
// Individual indexes retained for single-column queries (findByTrainerEmail, deleteBy).
@Entity
@Table(name = "trainer_batch_map", indexes = {
    @Index(name = "idx_tbm_trainer_email",   columnList = "trainerEmail"),
    @Index(name = "idx_tbm_batch_id",        columnList = "batchId"),
    @Index(name = "idx_tbm_org_id",          columnList = "organization_id"),
    @Index(name = "idx_tbm_email_batch",     columnList = "trainerEmail, batchId"),
    @Index(name = "idx_tbm_email_batch_org", columnList = "trainerEmail, batchId, organization_id")
})
public class TrainerBatchMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trainerEmail;

    private Long batchId;

    @Column(name = "organization_id")
    private String organizationId;

    public TrainerBatchMap() {}

    public TrainerBatchMap(String trainerEmail, Long batchId) {
        this.trainerEmail   = trainerEmail;
        this.batchId        = batchId;
        this.organizationId = null;
    }

    public TrainerBatchMap(String trainerEmail, Long batchId, String organizationId) {
        this.trainerEmail   = trainerEmail;
        this.batchId        = batchId;
        this.organizationId = organizationId;
    }

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }

    public String getTrainerEmail()                { return trainerEmail; }
    public void setTrainerEmail(String e)          { this.trainerEmail = e; }

    public Long getBatchId()                       { return batchId; }
    public void setBatchId(Long b)                 { this.batchId = b; }

    public String getOrganizationId()              { return organizationId; }
    public void setOrganizationId(String orgId)    { this.organizationId = orgId; }
}