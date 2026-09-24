package com.lms.live_session.service;

import com.lms.live_session.entity.TexoraWebhookOutbox;
import com.lms.live_session.entity.WebhookOutboxStatus;
import com.lms.live_session.repository.TexoraWebhookOutboxRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;

@Component
public class TexoraWebhookSender {

    private final TexoraWebhookOutboxRepository outboxRepository;
    private final RestTemplate restTemplate;

    @Value("${texora.webhook.url}")
    private String webhookUrl;

    @Value("${texora.webhook.secret}")
    private String webhookSecret;

    private static final long[] BACKOFF_SECONDS = {30, 60, 300, 900, 3600, 10800, 21600};
    private static final long MAX_TOTAL_AGE_HOURS = 24;

    public TexoraWebhookSender(TexoraWebhookOutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(10_000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Scheduled(fixedRate = 15_000)
    public void dispatchPending() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
        List<TexoraWebhookOutbox> pending = outboxRepository
                .findByStatusAndNextAttemptAtLessThanEqual(WebhookOutboxStatus.PENDING, now);

        for (TexoraWebhookOutbox row : pending) {
            send(row);
        }
    }

    private void send(TexoraWebhookOutbox row) {
        long tsSeconds = Instant.now().getEpochSecond();
        String signedString = tsSeconds + "." + row.getPayloadJson();
        String signature;
        try {
            signature = sign(signedString);
        } catch (Exception e) {
            markDead(row, "Failed to sign payload: " + e.getMessage());
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-ILMOra-Event", row.getEventType());
        headers.set("X-ILMOra-Event-Id", row.getEventId());
        headers.set("X-ILMOra-Timestamp", String.valueOf(tsSeconds));
        headers.set("X-ILMOra-Signature", "sha256=" + signature);

        HttpEntity<String> request = new HttpEntity<>(row.getPayloadJson(), headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
            handleResponse(row, response.getStatusCode().value(), null);
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            handleResponse(row, e.getStatusCode().value(), e.getMessage());
        } catch (org.springframework.web.client.HttpServerErrorException e) {
            handleResponse(row, e.getStatusCode().value(), e.getMessage());
        } catch (RestClientException e) {
            handleResponse(row, 0, e.getMessage());
        }
    }

    private void handleResponse(TexoraWebhookOutbox row, int statusCode, String error) {
        if (statusCode == 200 || statusCode == 204) {
            row.setStatus(WebhookOutboxStatus.SENT);
            row.setSentAt(LocalDateTime.now(ZoneId.of("UTC")));
            outboxRepository.save(row);
            return;
        }

        if (statusCode == 400 || statusCode == 401) {
            markDead(row, "Non-retryable response " + statusCode + (error != null ? ": " + error : ""));
            return;
        }

        row.setAttempts(row.getAttempts() + 1);
        row.setLastError("HTTP " + statusCode + (error != null ? ": " + error : ""));

        long elapsedHours = Duration.between(row.getCreatedAt(), LocalDateTime.now(ZoneId.of("UTC"))).toHours();

        boolean is404 = statusCode == 404;
        long cutoffHours = is404 ? 1 : MAX_TOTAL_AGE_HOURS;

        if (elapsedHours >= cutoffHours) {
            markDead(row, "Exceeded retry window (" + cutoffHours + "h) — last error: " + row.getLastError());
            return;
        }

        int backoffIndex = Math.min(row.getAttempts() - 1, BACKOFF_SECONDS.length - 1);
        long delaySeconds = BACKOFF_SECONDS[backoffIndex];
        row.setNextAttemptAt(LocalDateTime.now(ZoneId.of("UTC")).plusSeconds(delaySeconds));
        outboxRepository.save(row);
    }

    private void markDead(TexoraWebhookOutbox row, String error) {
        row.setStatus(WebhookOutboxStatus.DEAD);
        row.setLastError(error);
        outboxRepository.save(row);
        System.err.println("[TexoraWebhookSender] DEAD: event " + row.getEventId()
                + " (" + row.getEventType() + ") for meeting " + row.getMeetingId() + " — " + error);
    }

    private String sign(String signedString) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sig = mac.doFinal(signedString.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(sig);
    }
}