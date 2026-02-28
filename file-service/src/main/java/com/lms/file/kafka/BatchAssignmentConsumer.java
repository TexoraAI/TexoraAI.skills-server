package com.lms.file.kafka;

import com.lms.file.event.BatchAssignmentEvent;
import com.lms.file.event.BatchLifecycleEvent;
import com.lms.file.model.BatchTrainer;
import com.lms.file.model.FileClassroomAccess;
import com.lms.file.repository.BatchTrainerRepository;
import com.lms.file.repository.FileClassroomAccessRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
@Component
public class BatchAssignmentConsumer {

    private final FileClassroomAccessRepository accessRepository;
    private final BatchTrainerRepository trainerRepository;

    public BatchAssignmentConsumer(FileClassroomAccessRepository accessRepository,
                                   BatchTrainerRepository trainerRepository) {
        this.accessRepository = accessRepository;
        this.trainerRepository = trainerRepository;
    }

    @KafkaListener(topics = "batch-assignment", groupId = "file-service-group")
    @Transactional
    public void consume(BatchAssignmentEvent event) {

        String type = event.getType();
        Long batchId = event.getBatchId();
        String email = event.getEmail();
        String role = event.getRole();

        System.out.println("📁 FILE EVENT -> " + type + " | batch=" + batchId + " | role=" + role + " | email=" + email);

        switch (type) {

            case "TRAINER_ASSIGNED" -> {
                trainerRepository.save(new BatchTrainer(batchId, email));
                System.out.println("Trainer stored for batch");
            }

            case "TRAINER_REMOVED" -> {
                trainerRepository.deleteById(batchId);
                accessRepository.deleteByBatchId(batchId);
            }

            case "STUDENT_ASSIGNED" -> {

                String trainerEmail = trainerRepository
                        .findByBatchId(batchId)
                        .map(BatchTrainer::getTrainerEmail)
                        .orElse(null);

                accessRepository.save(
                        new FileClassroomAccess(batchId, trainerEmail, email)
                );

                System.out.println("✅ Student access added with trainer " + trainerEmail);
            }

            case "STUDENT_REMOVED" -> {
                accessRepository.deleteByStudentEmailAndBatchId(email, batchId);
            }
        }
    }
}

