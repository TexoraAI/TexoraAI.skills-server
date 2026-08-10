package com.lms.video.repository;

import com.lms.video.model.FeaturedVideoTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeaturedVideoTranscriptRepository extends JpaRepository<FeaturedVideoTranscript, Long> {

    Optional<FeaturedVideoTranscript> findBySessionId(Long sessionId);
}