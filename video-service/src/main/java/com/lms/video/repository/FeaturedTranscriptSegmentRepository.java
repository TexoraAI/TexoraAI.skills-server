package com.lms.video.repository;

import com.lms.video.model.FeaturedTranscriptSegment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeaturedTranscriptSegmentRepository extends JpaRepository<FeaturedTranscriptSegment, Long> {

    List<FeaturedTranscriptSegment> findByTranscriptIdOrderByOrderIndexAsc(Long transcriptId);
}