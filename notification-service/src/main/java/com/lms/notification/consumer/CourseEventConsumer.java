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
public class CourseEventConsumer {

    private final NotificationService notificationService;
    private final StudentBatchMapRepository studentBatchRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    public CourseEventConsumer(NotificationService notificationService,
                               StudentBatchMapRepository studentBatchRepo) {
        this.notificationService = notificationService;
        this.studentBatchRepo    = studentBatchRepo;
    }

    // ✅ FIXED: String instead of Map
    @KafkaListener(topics = "course-events", groupId = "notification-service-group")
    public void onCourseEvent(String message) {

        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            String type = (String) event.get("type");

            if (!"COURSE_CREATED".equals(type)) return;

            String title = (String) payload.get("title");
            Long batchId = Long.valueOf(payload.get("batchId").toString());

            List<String> studentEmails = studentBatchRepo.findAllByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();

            if (studentEmails.isEmpty()) {
                System.out.println("No students found for batchId=" + batchId);
                return;
            }

            NotificationDTO dto = new NotificationDTO();
            dto.setType("NEW_COURSE");
            dto.setTitle("New Course Available");
            dto.setMessage("A new course has been added: " + title);
            dto.setTargetUserIds(studentEmails);

            notificationService.createAndPush(dto);

            System.out.println("✅ COURSE_CREATED notification → "
                    + studentEmails.size() + " students, batchId=" + batchId);

        } catch (Exception e) {
            System.err.println("CourseEventConsumer error: " + e.getMessage());
        }
    }

    // ✅ FIXED: String instead of Map
    @KafkaListener(topics = "course-lifecycle", groupId = "notification-service-group")
    public void onCourseLifecycle(String message) {

        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);

            String type = (String) event.get("type");
            Long courseId = Long.valueOf(event.get("courseId").toString());

            NotificationDTO dto = new NotificationDTO();

            if ("COURSE_UPDATED".equals(type)) {
                dto.setType("COURSE_UPDATED");
                dto.setTitle("Course Updated");
                dto.setMessage("A course you are enrolled in has been updated (ID: " + courseId + ")");
            } else if ("COURSE_DELETED".equals(type)) {
                dto.setType("COURSE_DELETED");
                dto.setTitle("Course Removed");
                dto.setMessage("A course has been removed (ID: " + courseId + ")");
            } else return;

            dto.setTargetUserIds(List.of());

            notificationService.createAndPush(dto);

            System.out.println("✅ " + type + " notification sent, courseId=" + courseId);

        } catch (Exception e) {
            System.err.println("CourseLifecycleConsumer error: " + e.getMessage());
        }
    }
}