package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.repository.StudentBatchMapRepository;
import com.lms.notification.repository.TrainerBatchMapRepository;
import com.lms.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AssessmentEventConsumer {

    private final NotificationService       notificationService;
    private final StudentBatchMapRepository studentBatchRepo;
    private final TrainerBatchMapRepository trainerBatchRepo;
    private final ObjectMapper              mapper = new ObjectMapper();

    public AssessmentEventConsumer(NotificationService       notificationService,
                                    StudentBatchMapRepository studentBatchRepo,
                                    TrainerBatchMapRepository trainerBatchRepo) {
        this.notificationService = notificationService;
        this.studentBatchRepo    = studentBatchRepo;
        this.trainerBatchRepo    = trainerBatchRepo;
    }

    @KafkaListener(topics = "assessment-events", groupId = "notification-service-group")
    public void onAssessmentEvent(String message) {
        try {
            Map<String, Object> event   = mapper.readValue(message, Map.class);
            String              type    = (String) event.get("type");
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            if ("ASSIGNMENT_CREATED".equals(type)) {
                handleAssignmentCreated(payload);

            } else if ("QUIZ_CREATED".equals(type)) {
                handleQuizCreated(payload);
            }
            else if ("CODING_PROBLEM_ASSIGNED".equals(type)) {   // ✅ ADD THIS
                handleCodingProblemAssigned(payload);
            } else if ("CODE_SUBMITTED".equals(type)) {             // ✅ ADD THIS
                handleCodeSubmitted(payload);
            }

        } catch (Exception e) {
            System.err.println("AssessmentEventConsumer error: " + e.getMessage());
        }
    }

    // ── ASSIGNMENT_CREATED ─────────────────────────────────────────────────

    private void handleAssignmentCreated(Map<String, Object> payload) {

        String title  = (String) payload.get("title");
        Long batchId  = Long.valueOf(payload.get("batchId").toString());

        List<String> studentEmails = studentBatchRepo
        		.findAllByBatchId(batchId)
                .stream()
                .map(m -> m.getStudentEmail())
                .toList();

        if (studentEmails.isEmpty()) {
            System.out.println("No students found for batchId=" + batchId);
            return;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setType("NEW_ASSIGNMENT");
        dto.setTitle("New Assignment Posted");
        dto.setMessage("A new assignment has been posted: " + title);
        dto.setTargetUserIds(studentEmails);

        notificationService.createAndPush(dto);
        System.out.println("✅ ASSIGNMENT_CREATED notification → "
                + studentEmails.size() + " students, batchId=" + batchId);
    }

    // ── QUIZ_CREATED ───────────────────────────────────────────────────────

    private void handleQuizCreated(Map<String, Object> payload) {

        String title  = (String) payload.get("title");
        Long batchId  = Long.valueOf(payload.get("batchId").toString());

        List<String> studentEmails = studentBatchRepo
        		.findAllByBatchId(batchId)
                .stream()
                .map(m -> m.getStudentEmail())
                .toList();

        if (studentEmails.isEmpty()) {
            System.out.println("No students found for batchId=" + batchId);
            return;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setType("NEW_QUIZ");
        dto.setTitle("New Quiz Available");
        dto.setMessage("A new quiz has been posted: " + title);
        dto.setTargetUserIds(studentEmails);

        notificationService.createAndPush(dto);
        System.out.println("✅ QUIZ_CREATED notification → "
                + studentEmails.size() + " students, batchId=" + batchId);
    }
 // ── CODING_PROBLEM_ASSIGNED → students get notified ──────────────────
    private void handleCodingProblemAssigned(Map<String, Object> payload) {
        String title  = (String) payload.get("title");
        Long   batchId = Long.valueOf(payload.get("batchId").toString());
 
        List<String> studentEmails = studentBatchRepo
                .findAllByBatchId(batchId)
                .stream()
                .map(m -> m.getStudentEmail())
                .toList();
 
        if (studentEmails.isEmpty()) {
            System.out.println("No students for batchId=" + batchId);
            return;
        }
 
        NotificationDTO dto = new NotificationDTO();
        dto.setType("NEW_CODING_PROBLEM");
        dto.setTitle("New Coding Problem Assigned");
        dto.setMessage("A new coding problem has been assigned to you: " + title);
        dto.setTargetUserIds(studentEmails);
 
        notificationService.createAndPush(dto);
        System.out.println("✅ CODING_PROBLEM_ASSIGNED → " + studentEmails.size() + " students, batchId=" + batchId);
    }
 
    // ── CODE_SUBMITTED → trainer gets notified ────────────────────────────
    private void handleCodeSubmitted(Map<String, Object> payload) {
        String studentEmail = (String) payload.get("studentEmail");
        Long   batchId      = Long.valueOf(payload.get("batchId").toString());
        String status       = (String) payload.get("status"); // ✅ now works: producer sends .name()
 
        List<String> trainerEmails = trainerBatchRepo
                .findAllByBatchId(batchId)   // ✅ use findAllByBatchId not findById
                .stream()
                .map(m -> m.getTrainerEmail())
                .toList();
 
        if (trainerEmails.isEmpty()) {
            System.out.println("No trainer found for batchId=" + batchId);
            return;
        }
 
        NotificationDTO dto = new NotificationDTO();
        dto.setType("CODE_SUBMITTED");
        dto.setTitle("Student Submitted Code");
        dto.setMessage(studentEmail + " submitted a solution (status: " + status + ") in batch " + batchId);
        dto.setTargetUserIds(trainerEmails);
 
        notificationService.createAndPush(dto);
        System.out.println("✅ CODE_SUBMITTED → trainer notified, student=" + studentEmail);
    }
}