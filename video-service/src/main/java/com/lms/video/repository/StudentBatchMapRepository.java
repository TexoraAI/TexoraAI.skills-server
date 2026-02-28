package com.lms.video.repository;
import java.util.List;
import com.lms.video.model.StudentBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentBatchMapRepository extends JpaRepository<StudentBatchMap, Long> {
    Optional<StudentBatchMap> findByStudentEmail(String email);
    
    List<StudentBatchMap> findAllByStudentEmail(String email);

  
    void deleteByStudentEmailAndBatchId(String studentEmail, Long batchId);
    void deleteByBatchId(Long batchId);

}
