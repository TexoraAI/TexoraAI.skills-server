package com.lms.live_session.kafka;

import com.lms.live_session.event.LiveSessionEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LiveSessionProducer {

    // ✅ Object not String — same as batch producer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LiveSessionProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLiveStarted(LiveSessionEvent event) {
        kafkaTemplate.send("live-session-events", event); // ✅ send object directly
        System.out.println("✅ Kafka Event Sent: " + event.getSessionId());
    }
}