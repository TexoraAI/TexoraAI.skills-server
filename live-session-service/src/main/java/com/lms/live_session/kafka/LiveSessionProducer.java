package com.lms.live_session.kafka;

import com.lms.live_session.event.LiveSessionEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LiveSessionProducer {

    private final KafkaTemplate<String, LiveSessionEvent> kafkaTemplate;

    public LiveSessionProducer(KafkaTemplate<String, LiveSessionEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishLiveStarted(LiveSessionEvent event) {

        kafkaTemplate.send("live-session-events", event);

        System.out.println("✅ Kafka Event Sent: " + event.getSessionId());
    }
}