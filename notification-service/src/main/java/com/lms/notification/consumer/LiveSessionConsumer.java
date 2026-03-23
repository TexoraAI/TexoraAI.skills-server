package com.lms.notification.consumer;

import com.lms.notification.event.LiveSessionEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LiveSessionConsumer {

    @KafkaListener(
            topics = "live-session-events",
            groupId = "notification-service"
    )
    public void consume(LiveSessionEvent event) {

        System.out.println(
                "Live Session Started for batch: "
                        + event.getBatchId()
        );

        // Later send notification to students

    }

}