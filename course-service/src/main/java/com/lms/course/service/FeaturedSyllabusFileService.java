package com.lms.course.service;

import com.lms.course.kafka.FeaturedProgramKafkaProducer;
import com.lms.course.model.SessionFileStatus;
import com.lms.course.model.SyllabusSession;
import com.lms.course.repository.SyllabusSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class FeaturedSyllabusFileService {

    private final SyllabusSessionRepository syllabusSessionRepository;
    private final FeaturedProgramKafkaProducer kafkaProducer;

    public FeaturedSyllabusFileService(SyllabusSessionRepository syllabusSessionRepository,
                                        FeaturedProgramKafkaProducer kafkaProducer) {
        this.syllabusSessionRepository = syllabusSessionRepository;
        this.kafkaProducer = kafkaProducer;
    }

    // Called by the frontend right as it kicks off the direct upload to
    // file-service, so the UI can show a "processing" state immediately
    // instead of waiting on the FILE_READY Kafka round-trip.
    public SyllabusSession markProcessing(Long sessionId) {
        SyllabusSession session = getSessionOrThrow(sessionId);
        session.setFileStatus(SessionFileStatus.PROCESSING);
        return syllabusSessionRepository.save(session);
    }

    public SyllabusSession getFile(Long sessionId) {
        return getSessionOrThrow(sessionId);
    }

    public SyllabusSession deleteFile(Long sessionId) {
        SyllabusSession session = getSessionOrThrow(sessionId);

        String oldUrl = session.getFileUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            kafkaProducer.publishFeaturedFileDeleted(sessionId, oldUrl);
        }

        session.setFileId(null);
        session.setFileUrl(null);
        session.setFileName(null);
        session.setFileStatus(SessionFileStatus.NONE);
        return syllabusSessionRepository.save(session);
    }

    private SyllabusSession getSessionOrThrow(Long sessionId) {
        return syllabusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("SyllabusSession not found: " + sessionId));
    }
}