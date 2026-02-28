package com.lms.batch.repository;

import com.lms.batch.entity.Batch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    // OLD (keep)
    List<Batch> findByTrainerId(Long trainerId);

    // 🆕 NEW EMAIL BASED
    List<Batch> findByTrainerEmail(String trainerEmail);

    List<Batch> findByBranchId(Long branchId);

    Optional<Batch> findByIdAndTrainerId(Long id, Long trainerId);

    // 🆕 NEW EMAIL BASED AUTHORIZATION
    Optional<Batch> findByIdAndTrainerEmail(Long id, String trainerEmail);
}
