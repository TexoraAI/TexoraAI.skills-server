package com.lms.live_session.repository;

import com.lms.live_session.entity.TrainerBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {

    List<TrainerBatchMap> findByTrainerEmail(String trainerEmail);

    void deleteByBatchId(Long batchId);

    void deleteByTrainerEmailAndBatchId(String email, Long batchId);
}