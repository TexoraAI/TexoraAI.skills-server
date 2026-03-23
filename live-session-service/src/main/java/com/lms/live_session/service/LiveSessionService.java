package com.lms.live_session.service;

import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.event.LiveSessionEvent;
import com.lms.live_session.kafka.LiveSessionProducer;
import com.lms.live_session.repository.LiveSessionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LiveSessionService {

    private final LiveSessionRepository repository;
    private final LiveSessionProducer producer;

    public LiveSessionService(
            LiveSessionRepository repository,
            LiveSessionProducer producer
    ) {
        this.repository = repository;
        this.producer = producer;
    }

    public LiveSession createSession(LiveSession session) {
        session.setStatus("SCHEDULED");
        return repository.save(session);
    }

    public LiveSession startSession(Long id) {

        LiveSession session = repository.findById(id).orElseThrow();

        session.setStatus("LIVE");

        LiveSession saved = repository.save(session);

        // 🔥 KAFKA EVENT
        LiveSessionEvent event = new LiveSessionEvent(
                saved.getId(),
                saved.getBatchId(),
                saved.getTrainerId(),
                "STARTED"
        );

        producer.publishLiveStarted(event);

        return saved;
    }

    public LiveSession endSession(Long id) {

        LiveSession session = repository.findById(id).orElseThrow();

        session.setStatus("ENDED");

        return repository.save(session);
    }

    public List<LiveSession> getBatchSessions(Long batchId) {
        return repository.findByBatchId(batchId);
    }
    public List<LiveSession> getLiveSessions(Long batchId) {
        return repository.findByBatchIdAndStatus(batchId, "LIVE");
    }
}