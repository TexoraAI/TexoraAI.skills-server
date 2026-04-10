package com.lms.assessment.repository;

import com.lms.assessment.model.StudentBatchMap;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
public interface StudentBatchMapRepository extends JpaRepository<StudentBatchMap, Long> {

    boolean existsByStudentEmailAndBatchId(String email, Long batchId);

    void deleteByStudentEmailAndBatchId(String email, Long batchId);

    void deleteByBatchId(Long batchId);

    void deleteByStudentEmail(String email);
    
    
    @Query("SELECT sbm.batchId FROM StudentBatchMap sbm WHERE sbm.studentEmail = :email")
    Optional<Long> findBatchIdByStudentEmail(@Param("email") String email);
    
    List<StudentBatchMap> findByStudentEmail(String studentEmail);
    
    List<StudentBatchMap> findAllByStudentEmail(String studentEmail);
}
