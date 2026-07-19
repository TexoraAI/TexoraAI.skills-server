//package com.lms.assessment.repository;
//
//import com.lms.assessment.model.Assignment;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//
//import java.util.List;
//
//public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
//
//	@Query("""
//		    SELECT a FROM Assignment a
//		    WHERE a.id IN (
//		        SELECT sam.assignmentId
//		        FROM StudentAssignmentMap sam
//		        WHERE sam.studentEmail = :email
//		    )
//		""")
//		List<Assignment> findAssignmentsForStudent(String email);
//    List<Assignment> findByTrainerEmail(String trainerEmail);
//
//    List<Assignment> findByBatchId(Long batchId);  
//    
//    //added new code 
//    void deleteByBatchId(Long batchId);
//
//
//}
package com.lms.assessment.repository;

import com.lms.assessment.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    @Query("""
        SELECT a FROM Assignment a
        WHERE a.id IN (
            SELECT sam.assignmentId
            FROM StudentAssignmentMap sam
            WHERE sam.studentEmail = :email
        )
        """)
    List<Assignment> findAssignmentsForStudent(String email);

    List<Assignment> findByTrainerEmail(String trainerEmail);

    List<Assignment> findByBatchId(Long batchId);

    void deleteByBatchId(Long batchId);

    // 🏢 Multi-tenancy — org-scoped finders
    List<Assignment> findByOrganizationId(String organizationId);

    List<Assignment> findByOrganizationIdAndBatchId(String organizationId, Long batchId);

    Optional<Assignment> findByIdAndOrganizationId(Long id, String organizationId);

    boolean existsByIdAndOrganizationId(Long id, String organizationId);

    List<Assignment> findByOrganizationIdAndTrainerEmail(String organizationId, String trainerEmail);

    void deleteByOrganizationId(String organizationId);
    
    List<Assignment> findByOrganizationIdIsNull();
}