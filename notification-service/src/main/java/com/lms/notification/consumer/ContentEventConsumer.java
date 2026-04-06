package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.service.NotificationService;
import com.lms.notification.repository.StudentBatchMapRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ContentEventConsumer {

    private final NotificationService notificationService;
    private final StudentBatchMapRepository studentBatchRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public ContentEventConsumer(NotificationService notificationService,
                                StudentBatchMapRepository studentBatchRepo) {
        this.notificationService = notificationService;
        this.studentBatchRepo    = studentBatchRepo;
    }

    // ✅ FIX: String instead of Map
    @KafkaListener(topics = "content-events", groupId = "notification-service-group")
    public void onContentEvent(String message) {

        try {
            // ✅ Convert JSON string → Map
            Map<String, Object> event = mapper.readValue(message, Map.class);
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            String type = (String) event.get("type");

            if (!"CONTENT_CREATED".equals(type)) return;

            String title = (String) payload.get("title");

            Object batchIdRaw = payload.get("batchId");
            if (batchIdRaw == null) {
                System.out.println("⚠️ batchId missing from CONTENT_CREATED event — skipping");
                return;
            }

            Long batchId = Long.valueOf(batchIdRaw.toString());

            List<String> studentEmails = studentBatchRepo.findAllByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();

            if (studentEmails.isEmpty()) {
                System.out.println("No students found for batchId=" + batchId);
                return;
            }

            NotificationDTO dto = new NotificationDTO();
            dto.setType("NEW_CONTENT");
            dto.setTitle("New Content Added");
            dto.setMessage("New content has been uploaded: " + title);
            dto.setTargetUserIds(studentEmails);

            notificationService.createAndPush(dto);

            System.out.println("✅ CONTENT_CREATED notification → "
                    + studentEmails.size() + " students, batchId=" + batchId);

        } catch (Exception e) {
            System.err.println("ContentEventConsumer error: " + e.getMessage());
        }
    }
}