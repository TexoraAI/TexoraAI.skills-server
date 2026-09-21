package com.lms.live_session.repository;

import java.util.Optional;
import java.util.List;
import com.lms.live_session.entity.TrainerBatchMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerBatchMapRepository extends JpaRepository<TrainerBatchMap, Long> {
    List<TrainerBatchMap> findByTrainerEmail(String trainerEmail);
    void deleteByBatchId(Long batchId);
    void deleteByTrainerEmailAndBatchId(String email, Long batchId);

    List<TrainerBatchMap> findByBatchId(Long batchId);

    // NEW — resolves THIS specific trainer's mapping row for this batch.
    // Replaces findFirstByBatchId for org-validation use — that method picked
    // an arbitrary row when multiple trainers share a batch, which could
    // validate against the wrong trainer's org.
    Optional<TrainerBatchMap> findByTrainerEmailAndBatchId(String trainerEmail, Long batchId);

    // Kept — still valid for cases where "any trainer on this batch" is
    // genuinely what's needed (none currently, but not removing to avoid
    // breaking anything unseen). Just no longer used for org validation.
    Optional<TrainerBatchMap> findFirstByBatchId(Long batchId);
}