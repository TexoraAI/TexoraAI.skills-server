package com.lms.chat.service;
import java.util.HashMap;
import java.util.Map;
import com.lms.chat.entity.ChatClassroomAccess;
import com.lms.chat.entity.ChatMessage;
import com.lms.chat.kafka.ChatEventProducer;
import com.lms.chat.repository.ChatClassroomAccessRepository;
import com.lms.chat.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class ChatService {

   private final ChatMessageRepository repository;
    private final ChatClassroomAccessRepository accessRepo;
    private final ChatEventProducer chatEventProducer;

    public ChatService(ChatMessageRepository repository,
                       ChatClassroomAccessRepository accessRepo,
                       ChatEventProducer chatEventProducer) {
        this.repository = repository;
        this.accessRepo = accessRepo;
        this.chatEventProducer = chatEventProducer;
    }

    // ================= ORG-AWARE OVERLOAD =================
    // Called by the controller with organizationId from the JWT.
    // If organizationId == null (standalone user) → delegates straight through,
    // preserving existing behavior exactly.
    // If organizationId != null (org-based user) → runs an extra tenant check
    // BEFORE the existing relationship check, then delegates to the untouched
    // original method so all existing logic (save, Kafka event, etc.) is reused as-is.
    public ChatMessage send(ChatMessage msg, String loggedUser, String organizationId) {
        if (organizationId != null) {
            boolean allowedInOrg =
                    accessRepo.existsByOrganizationIdAndBatchIdAndTrainerEmailAndStudentEmail(
                            organizationId, msg.getBatchId(), msg.getReceiverEmail(), loggedUser
                    )
                    ||
                    accessRepo.existsByOrganizationIdAndBatchIdAndTrainerEmailAndStudentEmail(
                            organizationId, msg.getBatchId(), loggedUser, msg.getReceiverEmail()
                    );

            if (!allowedInOrg) {
                throw new RuntimeException("Cross-organization access denied: batch does not belong to your organization");
            }
        }
        return send(msg, loggedUser);
    }

    public ChatMessage send(ChatMessage msg, String loggedUser) {
        msg.setSenderEmail(loggedUser);

        // ✅ FIX: access check covers BOTH directions correctly
        // Case 1: loggedUser is STUDENT → trainer=receiverEmail, student=loggedUser
        // Case 2: loggedUser is TRAINER → trainer=loggedUser,    student=receiverEmail
        boolean allowed =
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(
                        msg.getBatchId(),
                        msg.getReceiverEmail(), // trainer
                        loggedUser             // student
                )
                ||
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(
                        msg.getBatchId(),
                        loggedUser,             // trainer
                        msg.getReceiverEmail()  // student
                );

        if (!allowed) {
            throw new RuntimeException("You are not assigned to this classroom");
        }

        msg.setSentAt(LocalDateTime.now());
        ChatMessage saved = repository.save(msg);

        // ✅ Fire Kafka event — receiver gets real-time notification
        try {
            chatEventProducer.sendMessageReceivedEvent(
                    saved.getBatchId(),
                    saved.getSenderEmail(),
                    saved.getReceiverEmail(),
                    saved.getMessage()
            );
        } catch (Exception e) {
            System.out.println("Kafka down. Chat saved without event: " + e.getMessage());
        }

        return saved;
    }

    public List<ChatMessage> getConversation(Long batchId, String me, String other) {
        boolean allowed =
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(batchId, me, other)
                ||
                accessRepo.existsByBatchIdAndTrainerEmailAndStudentEmail(batchId, other, me);
        if (!allowed)
            throw new RuntimeException("Access denied");
        return repository.getConversation(batchId, me, other);
    }

    // Org-aware overload — see send(...) overload above for the pattern rationale.
    public List<ChatMessage> getConversation(Long batchId, String me, String other, String organizationId) {
        if (organizationId != null) {
            boolean allowedInOrg =
                    accessRepo.existsByOrganizationIdAndBatchIdAndTrainerEmailAndStudentEmail(organizationId, batchId, me, other)
                    ||
                    accessRepo.existsByOrganizationIdAndBatchIdAndTrainerEmailAndStudentEmail(organizationId, batchId, other, me);
            if (!allowedInOrg)
                throw new RuntimeException("Cross-organization access denied: batch does not belong to your organization");
        }
        return getConversation(batchId, me, other);
    }

    public List<String> getTrainerStudents(Long batchId, String trainerEmail) {
        return accessRepo.findStudentsOfTrainer(batchId, trainerEmail);
    }

    // Org-aware overload — scopes the lookup to the trainer's organization
    public List<String> getTrainerStudents(Long batchId, String trainerEmail, String organizationId) {
        if (organizationId != null) {
            return accessRepo.findStudentsOfTrainer(batchId, trainerEmail, organizationId);
        }
        return getTrainerStudents(batchId, trainerEmail);
    }

    public String getStudentTrainer(Long batchId, String studentEmail) {
        return accessRepo
                .findTrainerForStudent(batchId, studentEmail)
                .orElseThrow(() ->
                        new RuntimeException("Trainer not assigned to this student"));
    }

    // Org-aware overload — scopes the lookup to the student's organization
    public String getStudentTrainer(Long batchId, String studentEmail, String organizationId) {
        if (organizationId != null) {
            return accessRepo
                    .findTrainerForStudent(batchId, studentEmail, organizationId)
                    .orElseThrow(() ->
                            new RuntimeException("Trainer not assigned to this student in your organization"));
        }
        return getStudentTrainer(batchId, studentEmail);
    }

    public Map<String, Object> getStudentContext(String studentEmail) {
        ChatClassroomAccess access = accessRepo
                .findStudentIgnoreCase(studentEmail)
                .orElseThrow(() -> new RuntimeException("No classroom assigned"));
        Map<String, Object> res = new HashMap<>();
        res.put("batchId", access.getBatchId());
        res.put("trainerEmail", access.getTrainerEmail());
        return res;
    }

    // Org-aware overload — verifies the resolved classroom actually belongs to the
    // student's organization before returning it. Response shape is unchanged.
    public Map<String, Object> getStudentContext(String studentEmail, String organizationId) {
        ChatClassroomAccess access = accessRepo
                .findStudentIgnoreCase(studentEmail)
                .orElseThrow(() -> new RuntimeException("No classroom assigned"));

        if (organizationId != null
                && access.getOrganizationId() != null
                && !organizationId.equals(access.getOrganizationId())) {
            throw new RuntimeException("Cross-organization access denied: classroom does not belong to your organization");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("batchId", access.getBatchId());
        res.put("trainerEmail", access.getTrainerEmail());
        return res;
    }
}