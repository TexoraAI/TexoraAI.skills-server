package com.lms.video.kafka;

import com.lms.video.model.StudentBatchMap;
import com.lms.video.model.TrainerBatchMap;
import com.lms.video.repository.StudentBatchMapRepository;
import com.lms.video.repository.TrainerBatchMapRepository;
import com.lms.video.repository.VideoRepository;

import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final TrainerBatchMapRepository trainerRepo;
    private final StudentBatchMapRepository studentRepo;
    private final VideoRepository videoRepo;

    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
                                   StudentBatchMapRepository studentRepo,
    	VideoRepository videoRepo) {
    	this.videoRepo=videoRepo;
        this.trainerRepo = trainerRepo;
        this.studentRepo = studentRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-assignment", groupId = "video-service-group")
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");
        String email = (String) event.get("email");
        Long batchId = ((Number) event.get("batchId")).longValue();

        System.out.println("📥 VIDEO SERVICE EVENT -> " + type + " | " + email + " | " + batchId);

        switch (type) {

            case "TRAINER_ASSIGNED" ->
                    trainerRepo.save(new TrainerBatchMap(email, batchId));

            case "STUDENT_ASSIGNED" ->
                    studentRepo.save(new StudentBatchMap(email, batchId));

            case "STUDENT_REMOVED" ->
                    studentRepo.deleteByStudentEmailAndBatchId(email, batchId);

            case "TRAINER_REMOVED" -> {

                // 1. delete trainer mapping
                trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);

                // 2. safety cleanup (remove any remaining students)
                studentRepo.deleteByBatchId(batchId);

                // 3. delete trainer videos of this batch
                videoRepo.deleteByBatchId(batchId);

                System.out.println("🧹 FULL TRAINER CLASSROOM CLEANED");
            }

        }
    }
}
