package com.lms.attendance.repository;

import com.lms.attendance.entity.TrainerBatchAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerBatchAccessRepository
        extends JpaRepository<TrainerBatchAccess, Long> {

    Optional<TrainerBatchAccess> findByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);

    void deleteByBatchIdAndTrainerEmail(Long batchId, String trainerEmail);

    void deleteByBatchId(Long batchId);
}
