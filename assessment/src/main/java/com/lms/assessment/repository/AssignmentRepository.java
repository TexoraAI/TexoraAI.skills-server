package com.lms.assessment.repository;

import com.lms.assessment.model.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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
    
    //added new code 
    void deleteByBatchId(Long batchId);


}
