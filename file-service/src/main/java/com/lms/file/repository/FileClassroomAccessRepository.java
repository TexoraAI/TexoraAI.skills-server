package com.lms.file.repository;

import com.lms.file.model.FileClassroomAccess;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface FileClassroomAccessRepository extends JpaRepository<FileClassroomAccess, Long> {

    List<FileClassroomAccess> findByBatchId(Long batchId);

    List<FileClassroomAccess> findByTrainerEmailAndBatchId(String trainerEmail, Long batchId);

    void deleteByStudentEmailAndBatchId(String studentEmail, Long batchId);

    void deleteByTrainerEmailAndBatchId(String trainerEmail, Long batchId);

    void deleteByBatchId(Long batchId);
    Optional<FileClassroomAccess> findByStudentEmail(String email);
    
  
    boolean existsByStudentEmailAndBatchId(String email, Long batchId);
}
