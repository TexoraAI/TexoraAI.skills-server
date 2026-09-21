package com.lms.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.payment.config.RazorpayClient;
import com.lms.payment.config.RazorpayProperties;
import com.lms.payment.dto.InitiatePaymentRequest;
import com.lms.payment.dto.InitiatePaymentResponse;
import com.lms.payment.dto.PaymentStatusResponse;
import com.lms.payment.dto.RazorpayWebhookPayload;
import com.lms.payment.entity.OutboxEvent;
import com.lms.payment.entity.OutboxStatus;
import com.lms.payment.entity.Payment;
import com.lms.payment.entity.PaymentStatus;
import com.lms.payment.entity.WebhookEvent;
import com.lms.payment.exception.InvalidIdempotencyKeyException;
import com.lms.payment.exception.InvalidWebhookSignatureException;
import com.lms.payment.exception.PaymentNotFoundException;
import com.lms.payment.repository.OutboxEventRepository;
import com.lms.payment.repository.PaymentRepository;
import com.lms.payment.repository.WebhookEventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final PaymentRepository paymentRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final ObjectMapper objectMapper;

    @Value("${payment.reconcile.stale-after-minutes:10}")
    private long staleAfterMinutes;

    public PaymentService(PaymentRepository paymentRepository,
                           WebhookEventRepository webhookEventRepository,
                           OutboxEventRepository outboxEventRepository,
                           RazorpayClient razorpayClient,
                           RazorpayProperties razorpayProperties,
                           ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.razorpayClient = razorpayClient;
        this.razorpayProperties = razorpayProperties;
        this.objectMapper = objectMapper;
    }

    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest dto, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new InvalidIdempotencyKeyException("Idempotency-Key header is required");
        }

        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Payment payment = existing.get();
            return new InitiatePaymentResponse(
                    payment.getId(),
                    payment.getOrderId(),
                    payment.getAmount(),
                    "INR",
                    razorpayProperties.getKeyId(),
                    payment.getStatus().name()
            );
        }

        Map<String, Object> razorpayOrder = razorpayClient.createOrder(dto.getAmount(), idempotencyKey);
        String razorpayOrderId = String.valueOf(razorpayOrder.get("id"));

        Payment payment = new Payment();
        payment.setUserId(dto.getUserId());
        payment.setOrgId(dto.getOrgId());
        payment.setOrderId(razorpayOrderId);
        payment.setAmount(dto.getAmount());
        payment.setStatus(PaymentStatus.CREATED);
        payment.setPurpose(dto.getPurpose());
        payment.setReferenceId(dto.getReferenceId());
        payment.setIdempotencyKey(idempotencyKey);

        try {
            payment = paymentRepository.save(payment);
        } catch (DataIntegrityViolationException ex) {
            payment = paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> ex);
        }

        return new InitiatePaymentResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getAmount(),
                "INR",
                razorpayProperties.getKeyId(),
                payment.getStatus().name()
        );
    }

    @Transactional
    public void handleWebhook(String rawBody, String signature) {
        verifySignature(rawBody, signature);

        RazorpayWebhookPayload webhookPayload;
        try {
            webhookPayload = objectMapper.readValue(rawBody, RazorpayWebhookPayload.class);
        } catch (Exception ex) {
            throw new InvalidWebhookSignatureException("Unable to parse webhook payload");
        }

        String eventId = webhookPayload.getEventId();
        if (StringUtils.hasText(eventId) && webhookEventRepository.existsById(eventId)) {
            return;
        }

        RazorpayWebhookPayload.EntityData entity = webhookPayload.getPayload() != null
                && webhookPayload.getPayload().getPayment() != null
                ? webhookPayload.getPayload().getPayment().getEntity()
                : null;

        if (entity == null || !StringUtils.hasText(entity.getOrderId())) {
            throw new InvalidWebhookSignatureException("Webhook payload missing payment entity");
        }

        Payment payment = paymentRepository.findByOrderId(entity.getOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found for orderId: " + entity.getOrderId()));

        boolean captured = "captured".equalsIgnoreCase(entity.getStatus())
                || "payment.captured".equalsIgnoreCase(webhookPayload.getEvent());

        payment.setRazorpayPaymentId(entity.getId());
        payment.setStatus(captured ? PaymentStatus.CAPTURED : PaymentStatus.FAILED);
        paymentRepository.save(payment);

        if (StringUtils.hasText(eventId)) {
            webhookEventRepository.save(new WebhookEvent(eventId, LocalDateTime.now()));
        }

        String eventType = captured ? "payment.success" : "payment.failed";
        insertOutboxEvent(payment, eventType);
    }

    @Scheduled(fixedDelayString = "${payment.reconcile.fixed-delay-ms:300000}")
    @Transactional
    public void reconcilePendingPayments() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(staleAfterMinutes);
        List<Payment> stalePayments = paymentRepository.findByStatusAndCreatedAtBefore(
                PaymentStatus.CREATED, cutoff);

        for (Payment payment : stalePayments) {
            try {
                Map<String, Object> order = razorpayClient.fetchOrder(payment.getOrderId());
                String status = String.valueOf(order.get("status"));

                PaymentStatus resolved = switch (status) {
                    case "paid" -> PaymentStatus.CAPTURED;
                    case "created", "attempted" -> PaymentStatus.CREATED;
                    default -> PaymentStatus.FAILED;
                };

                if (resolved != payment.getStatus()) {
                    payment.setStatus(resolved);
                    paymentRepository.save(payment);

                    if (resolved != PaymentStatus.CREATED) {
                        insertOutboxEvent(payment,
                                resolved == PaymentStatus.CAPTURED ? "payment.success" : "payment.failed");
                    }
                }
            } catch (Exception ex) {
                log.warn("Reconcile failed -> orderId={} error={}", payment.getOrderId(), ex.getMessage());
            }
        }
    }

    public PaymentStatusResponse getStatusByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("No payment found for orderId: " + orderId));

        return new PaymentStatusResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getRazorpayPaymentId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPurpose(),
                payment.getUpdatedAt()
        );
    }

    private void insertOutboxEvent(Payment payment, String eventType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", payment.getUserId());
        payload.put("orgId", payment.getOrgId());
        payload.put("referenceId", payment.getReferenceId()); // FIX: was "courseId" — didn't match what PaymentEventConsumer reads
        payload.put("purpose", payment.getPurpose());

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setAggregateId(payment.getId());
            outboxEvent.setEventType(eventType);
            outboxEvent.setPayload(payloadJson);
            outboxEvent.setStatus(OutboxStatus.PENDING);

            outboxEventRepository.save(outboxEvent);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize outbox payload", ex);
        }
    }

    private void verifySignature(String rawBody, String signature) {
        if (!StringUtils.hasText(signature)) {
            throw new InvalidWebhookSignatureException("Missing X-Razorpay-Signature header");
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(
                    razorpayProperties.getWebhookSecret().getBytes(StandardCharsets.UTF_8),
                    HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            boolean matches = MessageDigest.isEqual(
                    hex.toString().getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));

            if (!matches) {
                throw new InvalidWebhookSignatureException("Webhook signature verification failed");
            }
        } catch (InvalidWebhookSignatureException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InvalidWebhookSignatureException("Unable to verify webhook signature");
        }
    }
}