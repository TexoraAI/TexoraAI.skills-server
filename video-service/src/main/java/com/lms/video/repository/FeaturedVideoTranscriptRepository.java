package com.lms.video.repository;

import com.lms.video.model.FeaturedVideoTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lms.video.model.TranscriptSourceType;
import java.util.Optional;

public interface FeaturedVideoTranscriptRepository extends JpaRepository<FeaturedVideoTranscript, Long> {

    Optional<FeaturedVideoTranscript> findBySessionId(Long sessionId);
    
    Optional<FeaturedVideoTranscript> findBySessionIdAndSourceType(Long sessionId, TranscriptSourceType sourceType);
}