
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
public class VideoEventConsumer {

    private final NotificationService notificationService;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public VideoEventConsumer(NotificationService notificationService,
                               StudentBatchMapRepository studentBatchMapRepository) {
        this.notificationService = notificationService;
        this.studentBatchMapRepository = studentBatchMapRepository;
    }

    @KafkaListener(topics = "video-uploaded", groupId = "notification-service-group")
    public void onVideoUploaded(String message) { // ✅ String — handles both old and new messages
        try {
            Map<String, Object> event   = mapper.readValue(message, Map.class);
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            String title  = (String) payload.get("title");
            Long batchId  = Long.valueOf(payload.get("batchId").toString());

            List<String> studentEmails = studentBatchMapRepository
                    .findAllByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();

            if (studentEmails.isEmpty()) return;

            NotificationDTO dto = new NotificationDTO();
            dto.setType("NEW_VIDEO");
            dto.setTitle("New Video Uploaded");
            dto.setMessage("A new video is available in your batch: " + title);
            dto.setTargetUserIds(studentEmails);

            notificationService.createAndPush(dto);
            System.out.println("✅ VIDEO notification sent to " + studentEmails.size() + " students");

        } catch (Exception e) {
            System.err.println("VideoEventConsumer error: " + e.getMessage());
        }
    }
}