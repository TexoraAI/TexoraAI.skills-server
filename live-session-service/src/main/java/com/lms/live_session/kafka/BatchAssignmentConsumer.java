package com.lms.live_session.kafka;

import com.lms.live_session.event.BatchAssignmentEvent;
import com.lms.live_session.entity.StudentBatchMap;
import com.lms.live_session.entity.TrainerBatchMap;
import com.lms.live_session.repository.StudentBatchMapRepository;
import com.lms.live_session.repository.TrainerBatchMapRepository;

import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class BatchAssignmentConsumer {

    private final TrainerBatchMapRepository trainerRepo;
    private final StudentBatchMapRepository studentRepo;

    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
                                   StudentBatchMapRepository studentRepo) {
        this.trainerRepo = trainerRepo;
        this.studentRepo = studentRepo;
    }
    @KafkaListener(topics = "batch-assignment", groupId = "live-session-group")
    public void consume(Object event) {
        System.out.println("🔥🔥 LIVE SERVICE RECEIVED: " + event);
    }
    @Transactional
    @KafkaListener(topics = "batch-assignment", groupId = "live-session-group")
    public void consume(BatchAssignmentEvent event) {

        System.out.println("📥 LIVE EVENT -> " + event.getType());

        switch (event.getType()) {

            case "TRAINER_ASSIGNED" ->
                    trainerRepo.save(new TrainerBatchMap(
                            event.getEmail(),
                            event.getBatchId()
                    ));

            case "STUDENT_ASSIGNED" ->
                    studentRepo.save(new StudentBatchMap(
                            event.getEmail(),
                            event.getBatchId()
                    ));

            case "STUDENT_REMOVED" ->
                    studentRepo.deleteByStudentEmailAndBatchId(
                            event.getEmail(),
                            event.getBatchId()
                    );

            case "TRAINER_REMOVED" -> {
                trainerRepo.deleteByTrainerEmailAndBatchId(
                        event.getEmail(),
                        event.getBatchId()
                );
                studentRepo.deleteByBatchId(event.getBatchId());
            }
        }

    }
}