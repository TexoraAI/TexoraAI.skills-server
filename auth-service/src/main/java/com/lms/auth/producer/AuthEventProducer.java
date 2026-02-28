package com.lms.auth.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.auth.event.AuthEvent;   // ✅ CORRECT PACKAGE
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuthEventProducer {

    private static final String TOPIC = "auth-events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 🔒 ONE METHOD ONLY — LOCK THIS
    public void sendEvent(AuthEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, json);
            System.out.println("✔ Sent Kafka Event: " + json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
