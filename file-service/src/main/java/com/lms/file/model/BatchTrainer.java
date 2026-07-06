//package com.lms.file.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "batch_trainer_map")
//public class BatchTrainer {
//
//    @Id
//    private Long batchId;
//
//    private String trainerEmail;
//
//    public BatchTrainer() {}
//
//    public BatchTrainer(Long batchId, String trainerEmail) {
//        this.batchId = batchId;
//        this.trainerEmail = trainerEmail;
//    }
//
//    public Long getBatchId() { return batchId; }
//    public void setBatchId(Long batchId) { this.batchId = batchId; }
//
//    public String getTrainerEmail() { return trainerEmail; }
//    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }
//}
package com.lms.file.model;

import jakarta.persistence.*;

@Entity
@Table(name = "batch_trainer_map")
public class BatchTrainer {

    @Id
    private Long batchId;

    private String trainerEmail;

    private String organizationId; // NEW — nullable for standalone batches

    public BatchTrainer() {}

    public BatchTrainer(Long batchId, String trainerEmail) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
    }

    // NEW — used by consumer when organizationId is present on the event
    public BatchTrainer(Long batchId, String trainerEmail, String organizationId) {
        this.batchId = batchId;
        this.trainerEmail = trainerEmail;
        this.organizationId = organizationId;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    // NEW
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}