package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.service.NotificationService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ChatEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ✅ FIX: String instead of Map
    @KafkaListener(topics = "chat-events", groupId = "notification-service-group")
    public void onMessageReceived(String message) {

        try {
            // ✅ Convert JSON → Map
            Map<String, Object> event = mapper.readValue(message, Map.class);
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            String senderEmail   = (String) payload.get("senderEmail");
            String receiverEmail = (String) payload.get("receiverEmail");
            String preview       = (String) payload.get("preview");
            
            
            if (senderEmail != null && senderEmail.equalsIgnoreCase(receiverEmail)) {
                System.out.println("⏭️ Skipping self-notification for: " + senderEmail);
                return;
            }

            NotificationDTO dto = new NotificationDTO();
            dto.setType("NEW_CHAT");
            dto.setTitle("New Message");
            dto.setMessage(senderEmail + " sent you a message: " + preview);
            dto.setTargetUserIds(List.of(receiverEmail));

            notificationService.createAndPush(dto);

            System.out.println("✅ CHAT notification → " + receiverEmail);

        } catch (Exception e) {
            System.err.println("ChatEventConsumer error: " + e.getMessage());
        }
    }
}