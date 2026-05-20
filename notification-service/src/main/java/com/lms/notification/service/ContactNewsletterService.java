

package com.lms.notification.service;

import com.lms.notification.dto.ContactMessageRequest;
import com.lms.notification.dto.NewsletterSubscribeRequest;
import com.lms.notification.model.ContactMessage;
import com.lms.notification.model.NewsletterSubscriber;
import com.lms.notification.repository.ContactMessageRepository;
import com.lms.notification.repository.NewsletterSubscriberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactNewsletterService {

    private static final Logger log = LoggerFactory.getLogger(ContactNewsletterService.class);

    private final String supportEmail;  // injected from spring.mail.username

    private final NewsletterSubscriberRepository newsletterRepo;
    private final ContactMessageRepository contactRepo;
    private final EmailService emailService;  // ✅ CORRECTED: Use EmailService (not EmailSenderService)

    // ── Constructor injection ─────────────────────────────────
    public ContactNewsletterService(
            NewsletterSubscriberRepository newsletterRepo,
            ContactMessageRepository contactRepo,
            EmailService emailService,  // ✅ CORRECTED: EmailService parameter
            @Value("${spring.mail.username}") String supportEmail) {
        this.newsletterRepo = newsletterRepo;
        this.contactRepo = contactRepo;
        this.emailService = emailService;  // ✅ CORRECTED: Store as emailService
        this.supportEmail = supportEmail;
        
        log.info("🔧 ContactNewsletterService initialized");
        log.info("   Support Email: {}", supportEmail);
    }

    // ═══════════════════════════════════════════════════════════
    //  NEWSLETTER
    // ═══════════════════════════════════════════════════════════

    /**
     * Subscribe an email to the newsletter.
     *
     * @return "SUBSCRIBED"          – new subscription saved
     *         "ALREADY_SUBSCRIBED"  – email already exists & active
     */
    @Transactional
    public String subscribe(NewsletterSubscribeRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        log.debug("📧 Newsletter subscribe request for: {}", email);

        if (newsletterRepo.existsByEmail(email)) {
            log.info("⚠️  Newsletter subscribe attempt – already subscribed: {}", email);
            return "ALREADY_SUBSCRIBED";
        }

        // Save to database
        try {
            NewsletterSubscriber subscriber = new NewsletterSubscriber(email, true);
            newsletterRepo.save(subscriber);
            log.info("✅ Newsletter subscriber saved to DB: {}", email);
        } catch (Exception e) {
            log.error("❌ Failed to save newsletter subscriber: {}", email, e);
            return "ERROR";
        }

        // Welcome email – non-fatal if it fails
        try {
            log.debug("📤 Sending welcome email to: {}", email);
            
            // ✅ CORRECTED: Use emailService instead of emailSenderService
            emailService.sendWelcomeEmail(email);
            
            log.info("✅ Welcome email triggered for: {}", email);
        } catch (Exception ex) {
            log.warn("⚠️  Welcome email failed (non-fatal) for {}: {}", email, ex.getMessage());
        }

        log.info("✅ New newsletter subscriber: {}", email);
        return "SUBSCRIBED";
    }

    /**
     * Soft-unsubscribe: mark active = false.
     */
//    @Transactional
//    public void unsubscribe(String email) {
//        email = email.trim().toLowerCase();
//        log.debug("🔌 Newsletter unsubscribe request: {}", email);
//        
//        newsletterRepo.findByEmail(email)
//            .ifPresent(sub -> {
//                sub.setActive(false);
//                newsletterRepo.save(sub);
//                log.info("✅ Newsletter unsubscribed: {}",email);
//            });
//    }
    @Transactional
    public void unsubscribe(String email) {
        String normalizedEmail = email.trim().toLowerCase();

        log.debug("🔌 Newsletter unsubscribe request: {}", normalizedEmail);

        newsletterRepo.findByEmail(normalizedEmail)
            .ifPresent(sub -> {
                sub.setActive(false);
                newsletterRepo.save(sub);
                log.info("✅ Newsletter unsubscribed: {}", normalizedEmail);
            });
    }
    // ═══════════════════════════════════════════════════════════
    //  CONTACT FORM
    // ═══════════════════════════════════════════════════════════

    /**
     * Persist a contact form submission, then send:
     *   1. Auto-reply to the user
     *   2. Alert to the support team
     */
    @Transactional
    public ContactMessage saveContactMessage(ContactMessageRequest req) {

        log.debug("📬 Contact form submission received");
        log.debug("   From: {} ({})", req.getFullName(), req.getEmail());

        ContactMessage msg = new ContactMessage(
            req.getFullName().trim(),
            req.getEmail().trim().toLowerCase(),
            req.getPhoneNumber(),
            req.getTopic(),
            req.getMessage().trim()
        );

        // Save to database
        ContactMessage saved;
        try {
            saved = contactRepo.save(msg);
            log.info("✅ Contact message saved to DB – ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("❌ Failed to save contact message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save contact message", e);
        }

        // Auto-reply to user – non-fatal
        try {
            log.debug("📤 Sending auto-reply email to: {}", saved.getEmail());
            
            // ✅ CORRECTED: Use emailService instead of emailSenderService
            String autoReplyHtml = buildAutoReplyEmail(saved);
            emailService.sendAutoReplyEmail(
                saved.getEmail(),
                "We received your message – ILM ORA Support",
                autoReplyHtml
            );
            
            log.info("✅ Auto-reply email sent to: {}", saved.getEmail());
        } catch (Exception ex) {
            log.warn("⚠️  Auto-reply email failed (non-fatal) for {}: {}", 
                saved.getEmail(), ex.getMessage());
        }

        // Internal alert to support team – non-fatal
        try {
            log.debug("📤 Sending support alert email to: {}", supportEmail);
            
            String alertSubject = "[New Contact] "
                + (saved.getTopic() != null ? saved.getTopic() : "General")
                + " – " + saved.getFullName();
            String alertHtml = buildSupportAlertEmail(saved);
            
            // ✅ CORRECTED: Use emailService instead of emailSenderService
            emailService.sendSupportAlertEmail(supportEmail, alertSubject, alertHtml);
            
            log.info("✅ Support alert email sent – new contact from: {}", saved.getFullName());
        } catch (Exception ex) {
            log.warn("⚠️  Support alert email failed (non-fatal): {}", ex.getMessage());
        }

        log.info("✅ Contact message processed – ID: {}, Email: {}", saved.getId(), saved.getEmail());
        return saved;
    }

    // ═══════════════════════════════════════════════════════════
    //  EMAIL TEMPLATE HELPERS
    // ═══════════════════════════════════════════════════════════

    private String buildAutoReplyEmail(ContactMessage msg) {
        String topic = msg.getTopic() != null ? msg.getTopic() : "General";
        String message = msg.getMessage();

        return "<html><body style=\"font-family:sans-serif;background:#fbeee0;padding:32px;\">"
             + "<div style=\"max-width:520px;margin:0 auto;background:#ffffff;"
             +      "border-radius:16px;padding:32px;\">"
             + "<h2 style=\"color:#1a2340;\">Hi " + escapeHtml(msg.getFullName()) + ",</h2>"
             + "<p style=\"color:#5a6173;\">We've received your message and our team will get "
             +    "back to you within <strong>24 hours</strong>.</p>"
             + "<div style=\"background:#fdf5ec;border:1px solid #e8d9c4;border-radius:12px;"
             +      "padding:16px;margin:20px 0;\">"
             + "<p style=\"margin:0;font-size:13px;color:#5a6173;\">"
             +    "<strong>Topic:</strong> " + escapeHtml(topic) + "<br/>"
             +    "<strong>Message:</strong> " + escapeHtml(message)
             + "</p></div>"
             + "<p style=\"color:#5a6173;\">If urgent, email us at "
             +    "<a href=\"mailto:" + escapeHtml(supportEmail) + "\" style=\"color:#F97316;\">"
             +    escapeHtml(supportEmail) + "</a></p>"
             + "<hr style=\"border:none;border-top:1px solid #e8d9c4;margin:24px 0;\"/>"
             + "<p style=\"font-size:11px;color:#8a93a8;\">ILM ORA · New Delhi, India</p>"
             + "</div></body></html>";
    }

    private String buildSupportAlertEmail(ContactMessage msg) {
        return "<html><body style=\"font-family:sans-serif;padding:24px;\">"
             + "<h3 style=\"color:#1a2340;\">🆕 New Contact Form Submission</h3>"
             + "<table style=\"border-collapse:collapse;width:100%;font-size:13px;\">"
             + row("Name", escapeHtml(msg.getFullName()))
             + row("Email", escapeHtml(msg.getEmail()))
             + row("Phone", msg.getPhoneNumber() != null ? escapeHtml(msg.getPhoneNumber()) : "—")
             + row("Topic", msg.getTopic() != null ? escapeHtml(msg.getTopic()) : "—")
             + row("Message", escapeHtml(msg.getMessage()))
             + row("Status", msg.getStatus().name())
             + "</table>"
             + "<hr style=\"border:none;border-top:1px solid #ddd;margin:16px 0;\"/>"
             + "<p style=\"font-size:11px;color:#666;\">Message ID: " + msg.getId() + "</p>"
             + "</body></html>";
    }

    // ── Helpers ───────────────────────────────────────────────

    private static String row(String label, String value) {
        return "<tr>"
             + "<td style=\"padding:8px 12px;color:#5a6173;white-space:nowrap;\">"
             +    "<strong>" + label + "</strong></td>"
             + "<td style=\"padding:8px 12px;\">" + value + "</td>"
             + "</tr>";
    }

    /** Escape HTML to prevent XSS in email body */
    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }
}