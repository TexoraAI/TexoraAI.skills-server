package com.lms.notification.service;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
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