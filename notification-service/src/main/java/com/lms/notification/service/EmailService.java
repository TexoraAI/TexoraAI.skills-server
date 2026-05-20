package com.lms.notification.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service
public class EmailService {
	private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
 // ═══════════════════════════════════════════════════════════════════════════
    //  GENERIC EMAIL METHOD - Used by all features
    // ═══════════════════════════════════════════════════════════════════════════
 
    /**
     * Generic email sender used by newsletter, contact form, and booking confirmations
     * 
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML body content
     */
    private void sendEmailInternal(String toEmail, String subject, String htmlContent) {
        try {
            log.debug("📧 Preparing email for: {} | Subject: {}", toEmail, subject);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
 
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
 
            mailSender.send(message);
            
            log.info("✅ EMAIL SENT SUCCESSFULLY");
            log.info("   To: {}", toEmail);
            log.info("   Subject: {}", subject);
 
        } catch (jakarta.mail.AuthenticationFailedException e) {
            log.error("❌ SMTP AUTHENTICATION FAILED");
            log.error("   Error: {}", e.getMessage());
            log.error("   💡 Check SMTP username/password in application.properties");
            
        } catch (Exception e) {
            log.error("❌ UNEXPECTED ERROR sending email to {}", toEmail);
            log.error("   Subject: {}", subject);
            log.error("   Error: {}", e.getMessage(), e);
        }
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    //  NEWSLETTER EMAILS
    // ═══════════════════════════════════════════════════════════════════════════
 
    /**
     * Send welcome email to new newsletter subscriber
     */
    public void sendWelcomeEmail(String toEmail) {
        log.info("📬 Sending welcome email to: {}", toEmail);
        
        String htmlContent = "<html><body style=\"font-family:sans-serif;background:#fbeee0;padding:32px;\">"
             + "<div style=\"max-width:520px;margin:0 auto;background:#ffffff;"
             +      "border-radius:16px;padding:32px;\">"
             + "<h2 style=\"color:#1a2340;margin-bottom:8px;\">Welcome to "
             +    "<span style=\"color:#16a34a;\">ILM</span>"
             +    "<span style=\"color:#F97316;\"> ORA</span>!</h2>"
             + "<p style=\"color:#5a6173;\">You're now subscribed to our newsletter.</p>"
             + "<p style=\"color:#5a6173;\">You'll be the first to know about new courses, "
             +    "mentors, and platform updates.</p>"
             + "<hr style=\"border:none;border-top:1px solid #e8d9c4;margin:24px 0;\"/>"
             + "<p style=\"font-size:12px;color:#8a93a8;\">"
             +    "To unsubscribe, reply with UNSUBSCRIBE or visit your account settings."
             + "</p>"
             + "</div></body></html>";
 
        sendEmailInternal(toEmail, "Welcome to ILM ORA Newsletter!", htmlContent);
    }
 
    // ═══════════════════════════════════════════════════════════════════════════
    //  CONTACT FORM EMAILS
    // ═══════════════════════════════════════════════════════════════════════════
 
    /**
     * Send auto-reply to contact form submitter
     */
    public void sendAutoReplyEmail(String toEmail, String subject, String htmlContent) {
        log.info("📬 Sending auto-reply to: {}", toEmail);
        sendEmailInternal(toEmail, subject, htmlContent);
    }
 
    /**
     * Send alert to support team about new contact submission
     */
    public void sendSupportAlertEmail(String toEmail, String subject, String htmlContent) {
        log.info("📬 Sending support alert to: {}", toEmail);
        sendEmailInternal(toEmail, subject, htmlContent);
    }
    // ─────────────────────────────────────────────────────────────────
    // PUBLIC BOOKING CONFIRMATION
    // Sent immediately after a public user books a session
    // ─────────────────────────────────────────────────────────────────
    public void sendPublicBookingConfirmation(String toEmail, String fullName,
                                              String sessionTitle, String scheduledDate,
                                              String scheduledTime, Integer durationMinutes,
                                              String joinLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("✅ Booking Confirmed — " + sessionTitle);
            helper.setText(buildConfirmationHtml(
                    fullName, sessionTitle, scheduledDate,
                    scheduledTime, durationMinutes, joinLink
            ), true);

            mailSender.send(message);
            System.out.println("✅ Booking confirmation email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send booking confirmation to " + toEmail
                    + ": " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // PUBLIC 15-MIN REMINDER
    // Sent by scheduler 15 minutes before session starts
    // ─────────────────────────────────────────────────────────────────
    public void sendPublicSessionReminder(String toEmail, String fullName,
                                          String sessionTitle, String scheduledDate,
                                          String scheduledTime, String joinLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("⏰ Starting in 15 Minutes — " + sessionTitle);
            helper.setText(buildReminderHtml(
                    fullName, sessionTitle, scheduledDate, scheduledTime, joinLink
            ), true);

            mailSender.send(message);
            System.out.println("✅ 15-min reminder email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send reminder to " + toEmail
                    + ": " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // EMAIL TEMPLATES
    // ─────────────────────────────────────────────────────────────────

    private String buildConfirmationHtml(String fullName, String sessionTitle,
                                          String scheduledDate, String scheduledTime,
                                          Integer durationMinutes, String joinLink) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9fafb; padding: 32px; border-radius: 12px;">

              <div style="background: #1d4ed8; border-radius: 10px; padding: 28px; text-align: center; margin-bottom: 24px;">
                <h1 style="color: white; margin: 0; font-size: 24px;">🎉 You're In!</h1>
                <p style="color: #bfdbfe; margin: 8px 0 0;">Your spot has been confirmed</p>
              </div>

              <div style="background: white; border-radius: 10px; padding: 24px; margin-bottom: 16px;">
                <p style="color: #374151; font-size: 16px;">Hi <strong>%s</strong>,</p>
                <p style="color: #6b7280;">Your booking for the live session below is confirmed.
                   A join link has been included — save it!</p>

                <div style="background: #eff6ff; border-left: 4px solid #1d4ed8;
                            border-radius: 6px; padding: 16px; margin: 20px 0;">
                  <p style="margin: 0 0 6px; font-size: 18px; font-weight: bold; color: #1e3a8a;">%s</p>
                  <p style="margin: 0; color: #374151;">📅 %s &nbsp;|&nbsp; ⏰ %s &nbsp;|&nbsp; ⏱ %d min</p>
                </div>

                <div style="text-align: center; margin: 24px 0;">
                  <a href="%s"
                     style="background: #1d4ed8; color: white; padding: 14px 32px;
                            border-radius: 8px; text-decoration: none; font-weight: bold;
                            font-size: 16px; display: inline-block;">
                    Join Session →
                  </a>
                </div>

                <p style="color: #9ca3af; font-size: 13px; text-align: center;">
                  You'll also receive a reminder email 15 minutes before the session starts.
                </p>
              </div>

              <p style="color: #d1d5db; font-size: 12px; text-align: center; margin: 0;">
                TexoraAI.skills · If you didn't book this, please ignore this email.
              </p>
            </div>
            """.formatted(fullName, sessionTitle, scheduledDate,
                          scheduledTime, durationMinutes, joinLink);
    }

    private String buildReminderHtml(String fullName, String sessionTitle,
                                      String scheduledDate, String scheduledTime,
                                      String joinLink) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; background: #f9fafb; padding: 32px; border-radius: 12px;">

              <div style="background: #dc2626; border-radius: 10px; padding: 28px; text-align: center; margin-bottom: 24px;">
                <h1 style="color: white; margin: 0; font-size: 24px;">⏰ Starting in 15 Minutes!</h1>
                <p style="color: #fecaca; margin: 8px 0 0;">Your live session is about to begin</p>
              </div>

              <div style="background: white; border-radius: 10px; padding: 24px; margin-bottom: 16px;">
                <p style="color: #374151; font-size: 16px;">Hi <strong>%s</strong>,</p>
                <p style="color: #6b7280;">Your session is starting very soon. Click below to join now!</p>

                <div style="background: #fef2f2; border-left: 4px solid #dc2626;
                            border-radius: 6px; padding: 16px; margin: 20px 0;">
                  <p style="margin: 0 0 6px; font-size: 18px; font-weight: bold; color: #991b1b;">%s</p>
                  <p style="margin: 0; color: #374151;">📅 %s &nbsp;|&nbsp; ⏰ %s</p>
                </div>

                <div style="text-align: center; margin: 24px 0;">
                  <a href="%s"
                     style="background: #dc2626; color: white; padding: 14px 32px;
                            border-radius: 8px; text-decoration: none; font-weight: bold;
                            font-size: 16px; display: inline-block;">
                    Join Now →
                  </a>
                </div>
              </div>

              <p style="color: #d1d5db; font-size: 12px; text-align: center; margin: 0;">
                TexoraAI.skills · This is an automated reminder.
              </p>
            </div>
            """.formatted(fullName, sessionTitle, scheduledDate, scheduledTime, joinLink);
    }
}