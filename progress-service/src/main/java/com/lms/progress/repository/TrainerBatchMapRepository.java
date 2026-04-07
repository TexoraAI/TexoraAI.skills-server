package com.lms.progress.repository;

import com.lms.progress.model.TrainerBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {

    List<TrainerBatchMap> findByTrainerEmail(String email);
    List<TrainerBatchMap> findByBatchId(Long batchId);
    boolean existsByTrainerEmailAndBatchId(String email, Long batchId);
    void deleteByTrainerEmailAndBatchId(String email, Long batchId);
    void deleteByBatchId(Long batchId);
    void deleteByTrainerEmail(String email);
	
}