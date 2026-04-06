package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.service.NotificationService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AttendanceEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AttendanceEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ✅ FIX: String instead of Map
    @KafkaListener(topics = "attendance-events", groupId = "notification-service-group")
    public void onAttendanceMarked(String message) {

        try {
            // ✅ Convert JSON → Map
            Map<String, Object> event = mapper.readValue(message, Map.class);
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            String studentEmail = (String) payload.get("studentEmail");
            String trainerEmail = (String) payload.get("trainerEmail");
            String status       = (String) payload.get("status");
            String date         = (String) payload.get("date");

            NotificationDTO studentDTO = new NotificationDTO();
            studentDTO.setType("ATTENDANCE_MARKED");
            studentDTO.setTitle("Attendance Marked");
            studentDTO.setMessage("Your attendance on " + date + " has been marked as " + status);
            studentDTO.setTargetUserIds(List.of(studentEmail));

            notificationService.createAndPush(studentDTO);

            if ("ABSENT".equalsIgnoreCase(status) && trainerEmail != null) {
                NotificationDTO trainerDTO = new NotificationDTO();
                trainerDTO.setType("ATTENDANCE_PENDING");
                trainerDTO.setTitle("Student Absent");
                trainerDTO.setMessage(studentEmail + " was marked ABSENT on " + date);
                trainerDTO.setTargetUserIds(List.of(trainerEmail));

                notificationService.createAndPush(trainerDTO);
            }

            System.out.println("✅ ATTENDANCE notification → student: " 
                    + studentEmail + " | status: " + status);

        } catch (Exception e) {
            System.err.println("AttendanceEventConsumer error: " + e.getMessage());
        }
    }
}