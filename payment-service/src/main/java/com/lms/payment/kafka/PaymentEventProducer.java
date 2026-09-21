package com.lms.payment.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentSuccess(Map<String, Object> payload) {
        publish("payment.success", payload);
    }

    public void publishPaymentFailed(Map<String, Object> payload) {
        publish("payment.failed", payload);
    }

    public void publish(String eventType, Object payload) {
        try {
            kafkaTemplate.send(PaymentTopics.PAYMENT_EVENTS_TOPIC, eventType, payload)
                    .get(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new RuntimeException("Kafka publish failed for eventType=" + eventType, ex);
        }

        log.info("Published payment event -> eventType={} topic={} payload={}",
                eventType, PaymentTopics.PAYMENT_EVENTS_TOPIC, payload);
    }
}