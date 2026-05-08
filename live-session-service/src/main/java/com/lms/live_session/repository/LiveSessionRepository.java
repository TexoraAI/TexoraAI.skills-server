//package com.lms.live_session.repository;
//import java.time.LocalDate;
//import com.lms.live_session.entity.LiveSession;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//
//public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {
//
//    List<LiveSession> findByBatchId(Long batchId);
//   
//
//    List<LiveSession> findByBatchIdIn(List<Long> batchIds);
//
//    void deleteByBatchId(Long batchId);
//    List<LiveSession> findByBatchIdAndStatus(Long batchId, String status);
//    
//    
// // ✅ NEW: For scheduler
//    List<LiveSession> findByStatus(String status);
//    List<LiveSession> findByStatusAndScheduledDate(String status, LocalDate scheduledDate);
//    
//   
//}



package com.lms.live_session.repository;

import com.lms.live_session.entity.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LiveSessionRepository extends JpaRepository<LiveSession, Long> {

    List<LiveSession> findByBatchId(Long batchId);

    List<LiveSession> findByBatchIdIn(List<Long> batchIds);

    void deleteByBatchId(Long batchId);

    List<LiveSession> findByBatchIdAndStatus(Long batchId, String status);

    List<LiveSession> findByStatus(String status);

    List<LiveSession> findByStatusAndScheduledDate(String status, LocalDate scheduledDate);

    // ✅ Trainer-specific queries
    List<LiveSession> findByTrainerEmailOrderByScheduledDateDesc(String trainerEmail);

    List<LiveSession> findByTrainerEmailAndStatus(String trainerEmail, String status);

    List<LiveSession> findByTrainerEmailAndBatchId(String trainerEmail, Long batchId);
    
    List<LiveSession> findByStatusIn(List<String> statuses);
    
    
 // ADD these 2 methods to LiveSessionRepository

 // For calendar: trainer's sessions by date range
 List<LiveSession> findByTrainerEmailAndScheduledDateBetween(
     String trainerEmail, LocalDate start, LocalDate end);

 // For global published sessions (no batchId filter)
 List<LiveSession> findByIsPublishedTrueAndStatusIn(List<String> statuses);
}

