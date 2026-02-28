package com.lms.video.repository;
import java.util.List;
import com.lms.video.model.TrainerBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {
    Optional<TrainerBatchMap> findByTrainerEmailAndBatchId(String email, Long batchId);
    void deleteByTrainerEmailAndBatchId(String trainerEmail, Long batchId);
    void deleteByBatchId(Long batchId);
    List<TrainerBatchMap> findAllByTrainerEmail(String trainerEmail);

}
