
package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "chat_batch_trainer")
public class ChatBatchTrainer {

    @Id
    private Long batchId;

    private String trainerEmail;

    // Nullable: null for non-organization (standalone) users, set for org-based users
    private String organizationId;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}