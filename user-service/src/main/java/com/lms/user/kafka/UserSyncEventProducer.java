package com.lms.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.user.event.UserEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserSyncEventProducer {

    private static final String TOPIC = "auth-sync-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserSyncEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(UserEvent event) {
        try {
            kafkaTemplate.send(TOPIC, mapper.writeValueAsString(event));
            System.out.println("📤 USER → AUTH EVENT → " + event.getEventType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
