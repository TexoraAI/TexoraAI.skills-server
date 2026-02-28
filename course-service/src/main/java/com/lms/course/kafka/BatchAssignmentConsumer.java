package com.lms.course.kafka;

import com.lms.course.model.StudentBatchMap;
import com.lms.course.model.TrainerBatchMap;
import com.lms.course.repository.StudentBatchMapRepository;
import com.lms.course.repository.TrainerBatchMapRepository;

import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final TrainerBatchMapRepository trainerRepo;
    private final StudentBatchMapRepository studentRepo;

    public BatchAssignmentConsumer(
            TrainerBatchMapRepository trainerRepo,
            StudentBatchMapRepository studentRepo) {

        this.trainerRepo = trainerRepo;
        this.studentRepo = studentRepo;
    }

    @Transactional
    @KafkaListener(
            topics = "batch-assignment",
            groupId = "course-service-group"
    )
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");
        String email = (String) event.get("email");
        Long batchId = ((Number) event.get("batchId")).longValue();

        System.out.println("📥 COURSE SERVICE EVENT -> "
                + type + " | " + email + " | batch=" + batchId);

        switch (type) {

            case "TRAINER_ASSIGNED" -> {
                if (!trainerRepo.existsByTrainerEmailAndBatchId(email, batchId)) {
                    trainerRepo.save(new TrainerBatchMap(email, batchId));
                }
            }

            case "STUDENT_ASSIGNED" -> {
                if (!studentRepo.existsByStudentEmailAndBatchId(email, batchId)) {
                    studentRepo.save(new StudentBatchMap(email, batchId));
                }
            }

            case "STUDENT_REMOVED" ->
                    studentRepo.deleteByStudentEmailAndBatchId(
                            email, batchId
                    );

            case "TRAINER_REMOVED" ->
                    trainerRepo.deleteByTrainerEmailAndBatchId(
                            email, batchId
                    );
        }
    }
}