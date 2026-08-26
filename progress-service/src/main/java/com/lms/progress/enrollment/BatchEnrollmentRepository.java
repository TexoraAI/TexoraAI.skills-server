package com.lms.progress.enrollment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ASSUMPTION: adjust table/column names to match actual enrollment schema.
 *
 * Isolated, independent, read-only query surface used only by
 * RoadmapService#getStudentsProgress. Deliberately separate from any existing
 * batch/enrollment repository so this addition cannot collide with or alter
 * existing student_trainer_batch_map-style data access.
 */
public interface BatchEnrollmentRepository extends JpaRepository<BatchEnrollment, Long> {

    List<BatchEnrollment> findByTrainerUserId(Long trainerUserId);
}