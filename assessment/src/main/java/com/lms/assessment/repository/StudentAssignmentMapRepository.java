package com.lms.assessment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.lms.assessment.model.StudentAssignmentMap;

import java.util.List;
public interface StudentAssignmentMapRepository
extends JpaRepository<StudentAssignmentMap, Long> {

@Query("""
SELECT sam.assignmentId
FROM StudentAssignmentMap sam
WHERE sam.studentEmail = :email
""")
List<Long> findAssignmentIdsByStudent(String email);

boolean existsByAssignmentIdAndStudentEmail(
    Long assignmentId,
    String studentEmail
);
}