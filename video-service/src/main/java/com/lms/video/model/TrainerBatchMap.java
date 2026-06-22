//package com.lms.video.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "trainer_batch_map")
//public class TrainerBatchMap {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String trainerEmail;
//    private Long batchId;
// // TrainerBatchMap.java — ADD
//    private String organizationId;
//    public TrainerBatchMap() {}
//
//    public TrainerBatchMap(String trainerEmail, Long batchId) {
//        this.trainerEmail = trainerEmail;
//        this.batchId = batchId;
//    }
//    public TrainerBatchMap(String trainerEmail, Long batchId, String organizationId) {
//        this.trainerEmail = trainerEmail;
//        this.batchId = batchId;
//        this.organizationId = organizationId;
//    }
//
//    public String getTrainerEmail() { return trainerEmail; }
//    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }
//
//    public Long getBatchId() { return batchId; }
//    public void setBatchId(Long batchId) { this.batchId = batchId; }
//    
//    public String getOrganizationId() { return organizationId; }
//    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
//}
package com.lms.video.model;

import jakarta.persistence.*;

@Entity
@Table(name = "trainer_batch_map", indexes = {
        @Index(name = "idx_trainer_map_org", columnList = "organization_id")
})
public class TrainerBatchMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String trainerEmail;
    private Long batchId;

    // ✅ NEW — carried over from the batch-assignment Kafka event.
    // Used as a defense-in-depth cross-check independent of the video's own
    // organizationId.
    private String organizationId;

    public TrainerBatchMap() {}

    public TrainerBatchMap(String trainerEmail, Long batchId) {
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
    }

    // ✅ NEW — overload used once organizationId is available from the event
    public TrainerBatchMap(String trainerEmail, Long batchId, String organizationId) {
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
        this.organizationId = organizationId;
    }

    public String getTrainerEmail() { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    // ✅ NEW
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}