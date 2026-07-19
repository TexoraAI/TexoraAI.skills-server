//package com.lms.attendance.repository;
//
//import com.lms.attendance.entity.StudentBatchAccess;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//
//public interface StudentBatchAccessRepository
//        extends JpaRepository<StudentBatchAccess, Long> {
//
//    // validate student belongs to batch
//    Optional<StudentBatchAccess> findByBatchIdAndStudentUserId(
//            Long batchId,
//            Long studentUserId
//    );
//
//    // remove when student removed from batch
//    void deleteByBatchIdAndStudentEmail(Long batchId, String studentEmail);
//
//    // full batch cleanup
//    void deleteByBatchId(Long batchId);
//}

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

    // NEW — used when the authenticated student belongs to an organization
    Optional<StudentBatchAccess> findByBatchIdAndStudentUserIdAndOrganizationId(
            Long batchId,
            Long studentUserId,
            String organizationId
    );

    // NEW — used to validate a student's monthly-view access by email + organization
    Optional<StudentBatchAccess> findByStudentEmailAndOrganizationId(
            String studentEmail,
            String organizationId
    );

    // remove when student removed from batch
    void deleteByBatchIdAndStudentEmail(Long batchId, String studentEmail);

    // full batch cleanup
    void deleteByBatchId(Long batchId);
}