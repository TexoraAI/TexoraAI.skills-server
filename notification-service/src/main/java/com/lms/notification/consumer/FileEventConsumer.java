

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
public class FileEventConsumer {

    private final NotificationService notificationService;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public FileEventConsumer(NotificationService notificationService,
                             StudentBatchMapRepository studentBatchMapRepository) {
        this.notificationService = notificationService;
        this.studentBatchMapRepository = studentBatchMapRepository;
    }

    @KafkaListener(topics = "file-uploaded", groupId = "notification-service-group")
    public void onFileUploaded(String message) { // ✅ FIX: use String

        try {
            // ✅ Convert JSON String → Map
            Map<String, Object> event = mapper.readValue(message, Map.class);
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            String title = (String) payload.get("title");
            Long batchId = Long.valueOf(payload.get("batchId").toString());

            // ✅ Get students
            List<String> studentEmails = studentBatchMapRepository
                    .findAllByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();

            if (studentEmails.isEmpty()) return;

            // ✅ Create notification
            NotificationDTO dto = new NotificationDTO();
            dto.setType("NEW_FILE");
            dto.setTitle("New File Uploaded");
            dto.setMessage("A new file is available in your batch: " + title);
            dto.setTargetUserIds(studentEmails);

            // ✅ Push notification
            notificationService.createAndPush(dto);

            System.out.println("✅ FILE notification sent to " + studentEmails.size() + " students");

        } catch (Exception e) {
            System.err.println("FileEventConsumer error: " + e.getMessage());
        }
    }
}