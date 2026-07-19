//package com.lms.assessment.model;
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
//
//    public TrainerBatchMap() {}
//
//    public TrainerBatchMap(String trainerEmail, Long batchId) {
//        this.trainerEmail = trainerEmail;
//        this.batchId = batchId;
//    }
//
//    public String getTrainerEmail() { return trainerEmail; }
//    public Long getBatchId() { return batchId; }
//}
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

    // 🏢 Multi-tenancy — null for standalone users, expected.
    @Column(name = "organization_id")
    private String organizationId;

    public TrainerBatchMap() {}

    public TrainerBatchMap(String trainerEmail, Long batchId) {
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
    }

    public TrainerBatchMap(String trainerEmail, Long batchId, String organizationId) {
        this.trainerEmail = trainerEmail;
        this.batchId = batchId;
        this.organizationId = organizationId;
    }

    public String getTrainerEmail() { return trainerEmail; }
    public Long getBatchId() { return batchId; }
    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}