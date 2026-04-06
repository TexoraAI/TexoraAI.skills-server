package com.lms.notification.consumer;

import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.repository.StudentBatchMapRepository;
import com.lms.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class LiveSessionEventConsumer {

    private final NotificationService notificationService;
    private final StudentBatchMapRepository studentBatchRepo;

    // ✅ No ObjectMapper needed — Kafka deserializes directly to Map
    public LiveSessionEventConsumer(NotificationService notificationService,
                                     StudentBatchMapRepository studentBatchRepo) {
        this.notificationService = notificationService;
        this.studentBatchRepo    = studentBatchRepo;
    }

    @KafkaListener(topics = "live-session-events", groupId = "notification-service-group")
    public void onLiveSessionEvent(Map<String, Object> event) { // ✅ Map directly
        try {
            String status    = (String) event.get("status");
            Long   batchId   = Long.valueOf(event.get("batchId").toString());
            Long   sessionId = Long.valueOf(event.get("sessionId").toString());
            String timestamp = (String) event.get("timestamp");

            // ✅ only notify when session STARTED
            if (!"STARTED".equalsIgnoreCase(status)) {
                System.out.println("⏭️ Skipping live session event — status=" + status);
                return;
            }

            // ✅ find all students in this batch
            List<String> studentEmails = studentBatchRepo.findAllByBatchId(batchId)
                    .stream().map(m -> m.getStudentEmail()).toList();

            if (studentEmails.isEmpty()) {
                System.out.println("No students found for batchId=" + batchId);
                return;
            }

            NotificationDTO dto = new NotificationDTO();
            dto.setType("LIVE_SESSION_STARTED");
            dto.setTitle("Live Session Started!");
            dto.setMessage("Your trainer has started a live session. Join now! (Session ID: " + sessionId + ")");
            dto.setTargetUserIds(studentEmails);

            notificationService.createAndPush(dto);
            System.out.println("✅ LIVE_SESSION_STARTED notification → "
                    + studentEmails.size() + " students, batchId=" + batchId);

        } catch (Exception e) {
            System.err.println("LiveSessionEventConsumer error: " + e.getMessage());
        }
    }
}