package com.lms.progress.enrollment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ASSUMPTION: adjust table/column names to match actual enrollment schema.
 *
 * This is a minimal, read-only-facing mapping used solely to resolve "which
 * students are assigned to which trainer, in which batch" for
 * RoadmapService#getStudentsProgress. It is intentionally isolated from (and does
 * not touch, extend, or replace) any existing student_trainer_batch_map-style
 * entity/repository in the codebase. If a real enrollment entity already exists,
 * delete this file and point BatchEnrollmentRepository at it instead.
 */
@Entity
@Table(name = "batch_enrollment")
public class BatchEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long batchId;

    private Long studentUserId;

    private Long trainerUserId;

    public BatchEnrollment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getStudentUserId() {
        return studentUserId;
    }

    public void setStudentUserId(Long studentUserId) {
        this.studentUserId = studentUserId;
    }

    public Long getTrainerUserId() {
        return trainerUserId;
    }

    public void setTrainerUserId(Long trainerUserId) {
        this.trainerUserId = trainerUserId;
    }
}