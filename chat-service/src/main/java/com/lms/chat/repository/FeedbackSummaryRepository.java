package com.lms.chat.repository;

import com.lms.chat.entity.FeedbackSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackSummaryRepository extends JpaRepository<FeedbackSummary, Long> {

    Optional<FeedbackSummary> findByTrainerEmailAndBatchId(
            String trainerEmail, Long batchId);

    List<FeedbackSummary> findByTrainerEmailOrderByLastComputedAtDesc(
            String trainerEmail);

    List<FeedbackSummary> findByBatchIdOrderByOverallAvgRatingDesc(Long batchId);
}