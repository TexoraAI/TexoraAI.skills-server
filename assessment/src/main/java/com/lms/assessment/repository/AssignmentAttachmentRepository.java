package com.lms.assessment.repository;

import com.lms.assessment.model.AssignmentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssignmentAttachmentRepository 
        extends JpaRepository<AssignmentAttachment, Long> {

    List<AssignmentAttachment> findByAssignmentId(Long assignmentId);
    
    void deleteByAssignmentId(Long assignmentId);
}
