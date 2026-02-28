package com.lms.assessment.repository;

import com.lms.assessment.model.TrainerBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {

    boolean existsByTrainerEmailAndBatchId(String email, Long batchId);

    void deleteByTrainerEmailAndBatchId(String email, Long batchId);

    void deleteByBatchId(Long batchId);

    void deleteByTrainerEmail(String email);
    
    List<TrainerBatchMap> findByTrainerEmail(String email);

}
