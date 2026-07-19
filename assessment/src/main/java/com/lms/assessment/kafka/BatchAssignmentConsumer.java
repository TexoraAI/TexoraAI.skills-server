//package com.lms.assessment.kafka;
//
//import com.lms.assessment.model.StudentBatchMap;
//import com.lms.assessment.model.TrainerBatchMap;
//import com.lms.assessment.repository.StudentBatchMapRepository;
//import com.lms.assessment.repository.TrainerBatchMapRepository;
//
//import jakarta.transaction.Transactional;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//public class BatchAssignmentConsumer {
//
//    private final TrainerBatchMapRepository trainerRepo;
//    private final StudentBatchMapRepository studentRepo;
//
//    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
//                                   StudentBatchMapRepository studentRepo) {
//        this.trainerRepo = trainerRepo;
//        this.studentRepo = studentRepo;
//    }
//
//    @Transactional
//    @KafkaListener(topics = "batch-assignment", groupId = "assessment-service-group")
//    public void consume(Map<String, Object> event) {
//
//        String type = (String) event.get("type");
//        String email = (String) event.get("email");
//        Long batchId = ((Number) event.get("batchId")).longValue();
//
//        System.out.println("📥 ASSESSMENT EVENT -> " + type + " | " + email + " | " + batchId);
//
//        switch (type) {
//
//        case "STUDENT_ASSIGNED" -> {
//            if (!studentRepo.existsByStudentEmailAndBatchId(email, batchId)) {
//                studentRepo.save(new StudentBatchMap(email, batchId));
//            }
//        }
//
//        case "TRAINER_ASSIGNED" -> {
//            if (!trainerRepo.existsByTrainerEmailAndBatchId(email, batchId)) {
//                trainerRepo.save(new TrainerBatchMap(email, batchId));
//            }
//        }
//
//
//            case "STUDENT_REMOVED" ->
//                    studentRepo.deleteByStudentEmailAndBatchId(email, batchId);
//
//            case "TRAINER_REMOVED" -> {
//                trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);
//                studentRepo.deleteByBatchId(batchId); // classroom closed
//            }
//        }
//    }
//}
package com.lms.assessment.kafka;

import com.lms.assessment.model.StudentBatchMap;
import com.lms.assessment.model.TrainerBatchMap;
import com.lms.assessment.repository.StudentBatchMapRepository;
import com.lms.assessment.repository.TrainerBatchMapRepository;
import jakarta.transaction.Transactional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final TrainerBatchMapRepository trainerRepo;
    private final StudentBatchMapRepository studentRepo;

    public BatchAssignmentConsumer(TrainerBatchMapRepository trainerRepo,
                                   StudentBatchMapRepository studentRepo) {
        this.trainerRepo = trainerRepo;
        this.studentRepo = studentRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-assignment", groupId = "assessment-service-group")
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");
        String email = (String) event.get("email");
        Long batchId = ((Number) event.get("batchId")).longValue();

        // organizationId is null for non-organization (standalone) users — Batch
        // Service sends it as null in that case, so no extra null-check branching
        // is required here.
        Object orgIdRaw = event.get("organizationId");
        String organizationId = orgIdRaw == null ? null : orgIdRaw.toString();

        System.out.println("📥 ASSESSMENT EVENT -> " + type + " | " + email + " | " + batchId
                + " | org=" + organizationId);

        switch (type) {
            case "STUDENT_ASSIGNED" -> {
                if (!studentRepo.existsByStudentEmailAndBatchId(email, batchId)) {
                    StudentBatchMap map = new StudentBatchMap(email, batchId);
                    // TODO(batch-2): once StudentBatchMap has organizationId, prefer
                    // the value already stored against this batch's TrainerBatchMap
                    // record (source of truth), falling back to `organizationId` above.
                    // map.setOrganizationId(resolvedOrganizationId);
                    studentRepo.save(map);
                }
            }
            case "TRAINER_ASSIGNED" -> {
                if (!trainerRepo.existsByTrainerEmailAndBatchId(email, batchId)) {
                    TrainerBatchMap map = new TrainerBatchMap(email, batchId);
                    // TODO(batch-2): map.setOrganizationId(organizationId);
                    trainerRepo.save(map);
                }
            }
            case "STUDENT_REMOVED" ->
                    studentRepo.deleteByStudentEmailAndBatchId(email, batchId);
            case "TRAINER_REMOVED" -> {
                trainerRepo.deleteByTrainerEmailAndBatchId(email, batchId);
                studentRepo.deleteByBatchId(batchId); // classroom closed
            }
        }
    }
}