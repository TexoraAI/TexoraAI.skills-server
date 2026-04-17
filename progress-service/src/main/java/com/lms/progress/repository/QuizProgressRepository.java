package com.lms.progress.repository;

import com.lms.progress.model.QuizProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizProgressRepository
        extends JpaRepository<QuizProgress, Long> {

    Optional<QuizProgress>
    findByStudentEmailAndBatchId(String email, Long batchId);
    
    java.util.List<QuizProgress> findByBatchId(Long batchId);

}