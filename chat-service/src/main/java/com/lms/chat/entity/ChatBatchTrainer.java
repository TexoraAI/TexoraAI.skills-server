
package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "chat_batch_trainer")
public class ChatBatchTrainer {

    @Id
    private Long batchId;

    private String trainerEmail;

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }
}
