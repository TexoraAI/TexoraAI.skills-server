
package com.lms.chat.kafka;

import com.lms.chat.entity.ChatBatchTrainer;
import com.lms.chat.entity.ChatClassroomAccess;
import com.lms.chat.repository.ChatBatchTrainerRepository;
import com.lms.chat.repository.ChatClassroomAccessRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.Map;

@Service
public class BatchAssignmentConsumer {

    private final ChatClassroomAccessRepository accessRepo;
    private final ChatBatchTrainerRepository trainerRepo;

    public BatchAssignmentConsumer(
            ChatClassroomAccessRepository accessRepo,
            ChatBatchTrainerRepository trainerRepo) {
        this.accessRepo = accessRepo;
        this.trainerRepo = trainerRepo;
    }

    @Transactional
    @KafkaListener(topics = "batch-assignment", groupId = "chat-service-group")
    public void consume(Map<String, Object> event) {

        String type = (String) event.get("type");
        String email = (String) event.get("email");
        Long batchId = ((Number) event.get("batchId")).longValue();

        // organizationId is null for non-organization (standalone) users — Batch Service
        // sends it as null in that case, so no extra null-check branching is required here.
        Object orgIdRaw = event.get("organizationId");
        String organizationId = orgIdRaw == null ? null : orgIdRaw.toString();

        System.out.println("📩 CHAT EVENT -> " + type + " | " + email + " | batch=" + batchId
                + " | org=" + organizationId);

        switch (type) {

            // ================= TRAINER ASSIGNED =================
            case "TRAINER_ASSIGNED" -> {

                // store trainer
                ChatBatchTrainer trainer = new ChatBatchTrainer();
                trainer.setBatchId(batchId);
                trainer.setTrainerEmail(email);
                trainer.setOrganizationId(organizationId);
                trainerRepo.save(trainer);

                // update existing students
                accessRepo.attachTrainerToBatch(batchId, email);

                System.out.println("👨‍🏫 Trainer stored & linked to old students");
            }

            // ================= STUDENT ASSIGNED =================
            case "STUDENT_ASSIGNED" -> {

                String trainerEmail = trainerRepo
                        .findByBatchId(batchId)
                        .map(ChatBatchTrainer::getTrainerEmail)
                        .orElse(null);

                // Prefer the organizationId already stored against the batch's trainer
                // (source of truth for that batch); fall back to the event's value.
                String resolvedOrgId = trainerRepo
                        .findByBatchId(batchId)
                        .map(ChatBatchTrainer::getOrganizationId)
                        .orElse(organizationId);

                accessRepo.save(new ChatClassroomAccess(batchId, trainerEmail, email, resolvedOrgId));

                System.out.println("🎓 Student linked to trainer");
            }

            // ================= STUDENT REMOVED =================
            case "STUDENT_REMOVED" -> {
                accessRepo.deleteByStudentEmailAndBatchId(email, batchId);
                System.out.println("🚫 CHAT ACCESS REMOVED (student)");
            }

            // ================= TRAINER REMOVED =================
            case "TRAINER_REMOVED" -> {
                accessRepo.deleteByTrainerEmailAndBatchId(email, batchId);
                trainerRepo.findByBatchId(batchId).ifPresent(trainerRepo::delete);
                System.out.println("🚫 CHAT ACCESS REMOVED (trainer)");
            }
        }
    }
}