package com.lms.attendance.repository;

import com.lms.attendance.entity.StudentBatchAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentBatchAccessRepository
        extends JpaRepository<StudentBatchAccess, Long> {

    // validate student belongs to batch
    Optional<StudentBatchAccess> findByBatchIdAndStudentUserId(
            Long batchId,
            Long studentUserId
    );

    // remove when student removed from batch
    void deleteByBatchIdAndStudentEmail(Long batchId, String studentEmail);

    // full batch cleanup
    void deleteByBatchId(Long batchId);
}
