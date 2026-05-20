package com.lms.live_session.repository;

import com.lms.live_session.entity.AiTranscriptSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiTranscriptSegmentRepository extends JpaRepository<AiTranscriptSegment, Long> {
    List<AiTranscriptSegment> findByTranscriptSessionIdOrderByStartedAtSecondAsc(Long transcriptSessionId);
}