//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.ProblemAssignment;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface ProblemAssignmentRepository extends JpaRepository<ProblemAssignment, Long> {
//
//    // Used by ProblemAssignmentService.getAssignmentsByBatch()
//    List<ProblemAssignment> findByBatchIdAndActiveTrue(String batchId);
//
//    // Used by ProblemAssignmentService.assignProblem() — duplicate check
//    boolean existsByProblemIdAndBatchId(Long problemId, String batchId);
//
//    // Used to find a specific assignment
//    Optional<ProblemAssignment> findByProblemIdAndBatchId(Long problemId, String batchId);
//
//    // Trainer sees all assignments they made
//    List<ProblemAssignment> findByAssignedByEmailOrderByAssignedAtDesc(String trainerEmail);
//
//    // Count how many batches a problem is assigned to
//    long countByProblemIdAndActiveTrue(Long problemId);
//}
package com.lms.assessment.repository;

import com.lms.assessment.model.ProblemAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
@Repository
public interface ProblemAssignmentRepository extends JpaRepository<ProblemAssignment, Long> {

    // ✅ isActive — matches your Boolean field name in ProblemAssignment entity
    List<ProblemAssignment> findByBatchIdAndIsActiveTrue(String batchId);

    // ✅ isActive — same fix
    boolean existsByProblemIdAndBatchId(Long problemId, String batchId);

    Optional<ProblemAssignment> findByProblemIdAndBatchId(Long problemId, String batchId);

    List<ProblemAssignment> findByAssignedByEmailOrderByAssignedAtDesc(String trainerEmail);

    // ✅ isActive — same fix
    long countByProblemIdAndIsActiveTrue(Long problemId);
    
    void deleteByProblemId(Long problemId);
    
 // ADD these two — remove the old existsByProblemIdAndBatchId if present

    boolean existsByProblemIdAndBatchIdAndIsActiveTrue(Long problemId, String batchId);
}