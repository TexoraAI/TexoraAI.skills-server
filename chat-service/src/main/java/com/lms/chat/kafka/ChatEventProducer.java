package com.lms.chat.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class ChatEventProducer {

    // ✅ Object not String — same as batch producer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ChatEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessageReceivedEvent(Long batchId,
                                          String senderEmail,
                                          String receiverEmail,
                                          String messagePreview) {
        // ✅ No ObjectMapper — send Map directly
        Map<String, Object> payload = new HashMap<>();
        payload.put("batchId", batchId);
        payload.put("senderEmail", senderEmail);
        payload.put("receiverEmail", receiverEmail);
        payload.put("preview", messagePreview.length() > 60
                ? messagePreview.substring(0, 60) + "..."
                : messagePreview);

        Map<String, Object> event = new HashMap<>();
        event.put("type", "MESSAGE_RECEIVED");
        event.put("payload", payload);

        kafkaTemplate.send("chat-events", event); // ✅ send object directly
        System.out.println("🔥 CHAT EVENT -> MESSAGE_RECEIVED | receiver=" + receiverEmail);
    }
}