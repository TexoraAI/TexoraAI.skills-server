package com.lms.progress.repository;

import com.lms.progress.model.AssignmentProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssignmentProgressRepository
        extends JpaRepository<AssignmentProgress, Long> {

    Optional<AssignmentProgress>
    findByStudentEmailAndBatchId(String email, Long batchId);
    java.util.List<AssignmentProgress> findByBatchId(Long batchId);
}