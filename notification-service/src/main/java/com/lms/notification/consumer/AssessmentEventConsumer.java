package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.repository.StudentBatchMapRepository;
import com.lms.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AssessmentEventConsumer {

    private final NotificationService       notificationService;
    private final StudentBatchMapRepository studentBatchRepo;
    private final ObjectMapper              mapper = new ObjectMapper();

    public AssessmentEventConsumer(NotificationService       notificationService,
                                    StudentBatchMapRepository studentBatchRepo) {
        this.notificationService = notificationService;
        this.studentBatchRepo    = studentBatchRepo;
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
}