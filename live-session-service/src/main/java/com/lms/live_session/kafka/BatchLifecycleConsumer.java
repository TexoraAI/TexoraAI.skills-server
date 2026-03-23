package com.lms.live_session.kafka;

import com.lms.live_session.event.BatchLifecycleEvent;
import com.lms.live_session.repository.LiveSessionRepository;
import com.lms.live_session.repository.StudentBatchMapRepository;
import com.lms.live_session.repository.TrainerBatchMapRepository;

import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BatchLifecycleConsumer {

    private final StudentBatchMapRepository studentRepo;
    private final TrainerBatchMapRepository trainerRepo;
    private final LiveSessionRepository liveSessionRepo;

    public BatchLifecycleConsumer(StudentBatchMapRepository studentRepo,
                                  TrainerBatchMapRepository trainerRepo,
                                  LiveSessionRepository liveSessionRepo) {
        this.studentRepo = studentRepo;
        this.trainerRepo = trainerRepo;
        this.liveSessionRepo = liveSessionRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-lifecycle", groupId = "live-session-group")
    public void consume(BatchLifecycleEvent event) {

        System.out.println("🔥 LIFECYCLE EVENT -> " + event.getType());

        if ("BATCH_DELETED".equals(event.getType())) {

            Long batchId = event.getBatchId();

            studentRepo.deleteByBatchId(batchId);
            trainerRepo.deleteByBatchId(batchId);
            liveSessionRepo.deleteByBatchId(batchId);
        }
    }
    @KafkaListener(topics = "batch-assignment", groupId = "live-session-group")
    public void consume(Object event) {
        System.out.println("🔥🔥 LIVE SERVICE RECEIVED: " + event);
    }
}