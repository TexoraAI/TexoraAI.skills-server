package com.lms.live_session.repository;

import com.lms.live_session.entity.StudentBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentBatchMapRepository extends JpaRepository<StudentBatchMap, Long> {

    List<StudentBatchMap> findByStudentEmail(String studentEmail);

    void deleteByBatchId(Long batchId);

    void deleteByStudentEmailAndBatchId(String email, Long batchId);
}