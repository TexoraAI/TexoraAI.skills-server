//package com.lms.batch.entity;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "batch_trainer_student")
//public class BatchTrainerStudent {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private Long batchId;
//
//    @Column(nullable = false)
//    private String trainerEmail;
//
//    @Column(nullable = false)
//    private String studentEmail;
//
//    // ===== Constructors =====
//
//    public BatchTrainerStudent() {
//    }
//
//    public BatchTrainerStudent(Long batchId, String trainerEmail, String studentEmail) {
//        this.batchId = batchId;
//        this.trainerEmail = trainerEmail;
//        this.studentEmail = studentEmail;
//    }
//
//    // ===== Getters & Setters =====
//
//    public Long getId() {
//        return id;
//    }
//
//    public Long getBatchId() {
//        return batchId;
//    }
//
//    public void setBatchId(Long batchId) {
//        this.batchId = batchId;
//    }
//
//    public String getTrainerEmail() {
//        return trainerEmail;
//    }
//
//    public void setTrainerEmail(String trainerEmail) {
//        this.trainerEmail = trainerEmail;
//    }
//
//    public String getStudentEmail() {
//        return studentEmail;
//    }
//
//    public void setStudentEmail(String studentEmail) {
//        this.studentEmail = studentEmail;
//    }
//}
package com.lms.batch.entity;

import jakarta.persistence.*;

// OPTIMIZATION: Added composite and single-column indexes on all JOIN/WHERE columns.
// batchId, trainerEmail, studentEmail were unindexed — every findByBatchId was a full scan.
// idx_bts_batch_trainer_student covers the 3-column delete operation.
@Entity
@Table(name = "batch_trainer_student", indexes = {
    @Index(name = "idx_bts_batch_id",              columnList = "batchId"),
    @Index(name = "idx_bts_trainer_email",         columnList = "trainerEmail"),
    @Index(name = "idx_bts_student_email",         columnList = "studentEmail"),
    @Index(name = "idx_bts_batch_trainer",         columnList = "batchId, trainerEmail"),
    @Index(name = "idx_bts_batch_student",         columnList = "batchId, studentEmail"),
    @Index(name = "idx_bts_batch_trainer_student", columnList = "batchId, trainerEmail, studentEmail")
})
public class BatchTrainerStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long batchId;

    @Column(nullable = false)
    private String trainerEmail;

    @Column(nullable = false)
    private String studentEmail;

    public BatchTrainerStudent() {}

    public BatchTrainerStudent(Long batchId, String trainerEmail, String studentEmail) {
        this.batchId      = batchId;
        this.trainerEmail = trainerEmail;
        this.studentEmail = studentEmail;
    }

    public Long getId()                              { return id; }
    public Long getBatchId()                         { return batchId; }
    public void setBatchId(Long batchId)             { this.batchId = batchId; }
    public String getTrainerEmail()                  { return trainerEmail; }
    public void setTrainerEmail(String trainerEmail) { this.trainerEmail = trainerEmail; }
    public String getStudentEmail()                  { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }
}