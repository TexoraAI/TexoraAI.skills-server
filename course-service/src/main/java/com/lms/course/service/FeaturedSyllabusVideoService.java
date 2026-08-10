package com.lms.course.service;

import com.lms.course.kafka.FeaturedProgramKafkaProducer;
import com.lms.course.model.SessionVideoStatus;
import com.lms.course.model.SyllabusSession;
import com.lms.course.repository.SyllabusSessionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class FeaturedSyllabusVideoService {

    private final SyllabusSessionRepository syllabusSessionRepository;
    private final FeaturedProgramKafkaProducer featuredProgramKafkaProducer;

    // If a session sits in PROCESSING longer than this with no VIDEO_READY/FAILED
    // Kafka message arriving, treat it as failed on next read so it isn't stuck forever.
    private static final Duration PROCESSING_TIMEOUT = Duration.ofMinutes(15);

    public FeaturedSyllabusVideoService(SyllabusSessionRepository syllabusSessionRepository,
                                         FeaturedProgramKafkaProducer featuredProgramKafkaProducer) {
        this.syllabusSessionRepository = syllabusSessionRepository;
        this.featuredProgramKafkaProducer = featuredProgramKafkaProducer;
    }

    // ================= UPLOAD START (ack only, no file) =================
    // The actual file goes straight from the frontend to video-service now (no proxying
    // through course-service). This is just a lightweight ping so the admin UI can show
    // "processing" immediately, before the VIDEO_READY/FAILED Kafka message arrives.
    public SyllabusSession markUploadStarted(Long sessionId) {
        SyllabusSession session = syllabusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SyllabusSession not found: " + sessionId));

        session.setVideoStatus(SessionVideoStatus.PROCESSING);
        session.setVideoStatusUpdatedAt(Instant.now());
        return syllabusSessionRepository.save(session);
    }

    // ================= DELETE =================
    public void deleteVideo(Long sessionId) {
        SyllabusSession session = syllabusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SyllabusSession not found: " + sessionId));

        String oldUrl = session.getVideoUrl();
        if (oldUrl == null || oldUrl.isBlank()) {
            // nothing to delete
            return;
        }

        featuredProgramKafkaProducer.publishFeaturedVideoDeleted(sessionId, oldUrl);

        session.setVideoId(null);
        session.setVideoUrl(null);
        session.setVideoThumbnailUrl(null);
        session.setVideoDurationSeconds(null);
        session.setVideoStatus(SessionVideoStatus.NONE);
        session.setVideoStatusUpdatedAt(Instant.now());
        syllabusSessionRepository.save(session);
    }

    // ================= STATUS (with staleness self-heal) =================
    public Map<String, Object> getVideoStatus(Long sessionId) {
        SyllabusSession session = syllabusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SyllabusSession not found: " + sessionId));

        if (session.getVideoStatus() == SessionVideoStatus.PROCESSING
                && session.getVideoStatusUpdatedAt() != null
                && Duration.between(session.getVideoStatusUpdatedAt(), Instant.now()).compareTo(PROCESSING_TIMEOUT) > 0) {
            session.setVideoStatus(SessionVideoStatus.FAILED);
            session.setVideoStatusUpdatedAt(Instant.now());
            session = syllabusSessionRepository.save(session);
        }

        Map<String, Object> status = new HashMap<>();
        status.put("sessionId", session.getId());
        status.put("videoStatus", session.getVideoStatus());
        status.put("videoId", session.getVideoId());
        status.put("videoUrl", session.getVideoUrl());
        status.put("videoThumbnailUrl", session.getVideoThumbnailUrl());
        status.put("videoDurationSeconds", session.getVideoDurationSeconds());
        return status;
    }
}