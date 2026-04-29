package com.lms.assessment.repository;

import com.lms.assessment.model.CodeSubmission;
import com.lms.assessment.model.CodeSubmission.ExecutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CodeSubmissionRepository extends JpaRepository<CodeSubmission, Long> {

    List<CodeSubmission> findByStudentEmailOrderByCreatedAtDesc(String studentEmail);

    List<CodeSubmission> findByBatchIdOrderByCreatedAtDesc(String batchId);

    List<CodeSubmission> findByBatchIdAndStudentEmailOrderByCreatedAtDesc(
        String batchId, String studentEmail
    );

    List<CodeSubmission> findByBatchIdAndStatus(String batchId, ExecutionStatus status);

    long countByStudentEmailAndBatchId(String studentEmail, String batchId);
}