package com.lms.live_session.kafka;

import com.lms.live_session.event.SessionNotificationEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MeetingInviteProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MeetingInviteProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishMeetingInvite(SessionNotificationEvent event) {
        kafkaTemplate.send("meeting-invite-notifications", event);
        System.out.println("✅ Meeting invite event sent to: " + event.getRecipientEmail());
    }
}