package com.lms.notification.repository;
import com.lms.notification.model.StudentBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StudentBatchMapRepository
        extends JpaRepository<StudentBatchMap, Long> {
    List<StudentBatchMap> findAllByBatchId(Long batchId);
    List<StudentBatchMap> findAllByStudentEmail(String studentEmail);
    void deleteByStudentEmailAndBatchId(String email, Long batchId);
    void deleteByBatchId(Long batchId);
}