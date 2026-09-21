package com.lms.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.payment.entity.OutboxEvent;
import com.lms.payment.entity.OutboxStatus;
import com.lms.payment.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

// Relays PENDING OutboxEvent rows to Kafka on a fixed schedule and flips them to
// PUBLISHED on success. Rows are never deleted, so the table also serves as an
// audit trail of every domain event that was ever raised.
//
// A row that keeps failing (bad payload, permanently broken downstream, etc.) is
// capped at maxRetries and moved to FAILED instead of being retried forever — that
// turns a silent, endless retry loop into a visible dead-letter row for manual
// investigation/replay.
@Component
public class OutboxRelayJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayJob.class);

    private final OutboxEventRepository outboxEventRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${outbox.relay.max-retries:10}")
    private int maxRetries;

    public OutboxRelayJob(OutboxEventRepository outboxEventRepository,
                           PaymentEventProducer paymentEventProducer,
                           ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.paymentEventProducer = paymentEventProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:10000}")
    @Transactional
    public void relayPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByStatus(OutboxStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            try {
                Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
                paymentEventProducer.publish(event.getEventType(), payload);

                event.setStatus(OutboxStatus.PUBLISHED);
                event.setLastError(null);
                outboxEventRepository.save(event);
            } catch (Exception ex) {
                int attempts = event.getRetryCount() + 1;
                event.setRetryCount(attempts);
                event.setLastError(ex.getMessage());

                if (attempts >= maxRetries) {
                    event.setStatus(OutboxStatus.FAILED);
                    log.error("Outbox event exhausted retries, moved to FAILED (dead-letter) -> eventId={} eventType={} attempts={} error={}",
                            event.getId(), event.getEventType(), attempts, ex.getMessage());
                } else {
                    log.warn("Outbox relay failed, will retry -> eventId={} eventType={} attempt={}/{} error={}",
                            event.getId(), event.getEventType(), attempts, maxRetries, ex.getMessage());
                }

                outboxEventRepository.save(event);
            }
        }
    }
}