package com.lms.course.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lms.course.model.StudentBatchMap;
public interface StudentBatchMapRepository extends JpaRepository<StudentBatchMap, Long> {

List<StudentBatchMap> findByStudentEmail(String email);
void deleteByStudentEmailAndBatchId(String email, Long batchId);
void deleteByBatchId(Long batchId);
boolean existsByStudentEmailAndBatchId(String email, Long batchId);
}

