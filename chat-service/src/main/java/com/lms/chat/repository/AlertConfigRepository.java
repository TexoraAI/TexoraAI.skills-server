package com.lms.chat.repository;

import com.lms.chat.entity.AlertConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AlertConfigRepository extends JpaRepository<AlertConfig, Long> {
    
    Optional<AlertConfig> findByBatchId(Long batchId);
    
    Optional<AlertConfig> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);
}