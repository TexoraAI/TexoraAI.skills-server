package com.lms.notification.repository;
import com.lms.notification.model.TrainerBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerBatchMapRepository
        extends JpaRepository<TrainerBatchMap, Long> {
    void deleteByTrainerEmailAndBatchId(String email, Long batchId);
    void deleteByBatchId(Long batchId);
}