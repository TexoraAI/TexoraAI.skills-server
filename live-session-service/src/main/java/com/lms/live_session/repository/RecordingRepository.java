package com.lms.live_session.repository;

import com.lms.live_session.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RecordingRepository extends JpaRepository<Recording, Long> {

    List<Recording> findByBatchId(Long batchId);

    List<Recording> findBySessionId(Long sessionId);

    // ✅ Changed: trainerEmail instead of trainerId
    List<Recording> findByTrainerEmail(String trainerEmail);

    List<Recording> findByTrainerEmailAndBatchId(String trainerEmail, Long batchId);

    List<Recording> findByStatus(String status);

    List<Recording> findByBatchIdAndStatus(Long batchId, String status);

    List<Recording> findByRecordingType(String recordingType);

    boolean existsBySessionId(Long sessionId);

    @Modifying
    @Transactional
    @Query("UPDATE Recording r SET r.viewCount = r.viewCount + 1 WHERE r.id = :id")
    void incrementViewCount(@Param("id") Long id);

    List<Recording> findByBatchIdAndStatusOrderByCreatedAtDesc(Long batchId, String status);

    List<Recording> findAllByOrderByCreatedAtDesc();

    // ✅ Trainer's recordings ordered newest first
    List<Recording> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);
}