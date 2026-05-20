package com.lms.notification.controller;

import com.lms.notification.dto.ApiResponse;
import com.lms.notification.dto.ContactMessageRequest;
import com.lms.notification.dto.ContactMessageResponse;
import com.lms.notification.dto.NewsletterSubscribeRequest;
import com.lms.notification.model.ContactMessage;
import com.lms.notification.service.ContactNewsletterService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public endpoints for:
 *   POST   /api/v1/notification/newsletter/subscribe
 *   DELETE /api/v1/notification/newsletter/unsubscribe?email=
 *   POST   /api/v1/notification/contact
 *
 * No authentication required – these are public-facing forms.
 */
@RestController
@RequestMapping("/api/v1/notification")
public class ContactNewsletterController {

    private static final Logger log = LoggerFactory.getLogger(ContactNewsletterController.class);

    private final ContactNewsletterService service;

    // ── Constructor injection ─────────────────────────────────
    public ContactNewsletterController(ContactNewsletterService service) {
        this.service = service;
        log.info("🔧 ContactNewsletterController initialized");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  POST /api/v1/notification/newsletter/subscribe
    //  Body: { "email": "user@example.com" }
    //  201  – subscribed successfully
    //  409  – email already subscribed
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/newsletter/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @Valid @RequestBody NewsletterSubscribeRequest req) {

        log.info("📧 POST /api/v1/notification/newsletter/subscribe");
        log.info("   Email: {}", req.getEmail());

        String result = service.subscribe(req);

        if ("ALREADY_SUBSCRIBED".equals(result)) {
            log.warn("⚠️  Email already subscribed: {}", req.getEmail());
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("This email is already subscribed."));
        }

        if ("ERROR".equals(result)) {
            log.error("❌ Error subscribing: {}", req.getEmail());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process subscription. Please try again."));
        }

        log.info("✅ Newsletter subscription successful: {}", req.getEmail());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("✅ Successfully subscribed to ILM ORA newsletter!", null));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  DELETE /api/v1/notification/newsletter/unsubscribe?email=user@example.com
    //  200  – unsubscribed (or was already inactive – idempotent)
    // ═══════════════════════════════════════════════════════════════════════════

    @DeleteMapping("/newsletter/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @RequestParam String email) {

        log.info("🗑️  DELETE /api/v1/notification/newsletter/unsubscribe");
        log.info("   Email: {}", email);

        service.unsubscribe(email);

        log.info("✅ Unsubscribe successful: {}", email);
        return ResponseEntity.ok(
            ApiResponse.success("✅ Unsubscribed successfully.", null));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  POST /api/v1/notification/contact
    //  Body: ContactMessageRequest
    //  201  – message saved & emails sent
    //  400  – validation failure (handled by global exception handler)
    // ═══════════════════════════════════════════════════════════════════════════

    @PostMapping("/contact")
    public ResponseEntity<ApiResponse<ContactMessageResponse>> submitContact(
            @Valid @RequestBody ContactMessageRequest req) {

        log.info("📬 POST /api/v1/notification/contact");
        log.info("   Name: {}", req.getFullName());
        log.info("   Email: {}", req.getEmail());
        log.info("   Topic: {}", req.getTopic());

        try {
            ContactMessage saved = service.saveContactMessage(req);

            ContactMessageResponse responseBody = new ContactMessageResponse(
                saved.getId(),
                saved.getSubmittedAt(),
                saved.getStatus().name()
            );

            log.info("✅ Contact form processed successfully - ID: {}", saved.getId());
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "✅ Message sent! We'll get back to you within 24 hours.",
                    responseBody
                ));

        } catch (Exception e) {
            log.error("❌ Error processing contact form: {}", e.getMessage(), e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to process your message. Please try again."));
        }
    }
}