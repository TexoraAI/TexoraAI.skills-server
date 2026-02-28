package com.lms.file.repository;

import com.lms.file.model.BatchTrainer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BatchTrainerRepository extends JpaRepository<BatchTrainer, Long> {
    Optional<BatchTrainer> findByBatchId(Long batchId);
}
