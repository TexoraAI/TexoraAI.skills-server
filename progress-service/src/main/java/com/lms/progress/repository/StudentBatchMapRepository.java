package com.lms.progress.repository;

import com.lms.progress.model.StudentBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentBatchMapRepository extends JpaRepository<StudentBatchMap, Long> {

    List<StudentBatchMap> findByStudentEmail(String email);
    boolean existsByStudentEmailAndBatchId(String email, Long batchId);
    void deleteByStudentEmailAndBatchId(String email, Long batchId);
    void deleteByBatchId(Long batchId);
    
    List<StudentBatchMap> findByBatchId(Long batchId);   // ✅ needed for cleanup
}