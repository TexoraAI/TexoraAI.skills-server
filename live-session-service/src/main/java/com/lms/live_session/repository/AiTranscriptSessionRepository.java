package com.lms.live_session.repository;

import com.lms.live_session.entity.AiTranscriptSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiTranscriptSessionRepository extends JpaRepository<AiTranscriptSession, Long> {
    List<AiTranscriptSession> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);
}