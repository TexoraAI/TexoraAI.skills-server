package com.lms.assessment.repository;

import com.lms.assessment.model.AssignmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentSubmissionRepository
        extends JpaRepository<AssignmentSubmission, Long> {

	Optional<AssignmentSubmission> findByAssignmentIdAndStudentEmail(
	        Long assignmentId, String studentEmail);

	List<AssignmentSubmission> findByStudentEmail(String studentEmail);

	void deleteByAssignmentId(Long assignmentId);
	
    List<AssignmentSubmission>
    findByAssignmentId(Long assignmentId);
    
    
   


}
