//package com.lms.auth.service;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
//import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.ses.SesClient;
//import software.amazon.awssdk.services.ses.model.*;
//
//@Service
//public class SesEmailService {
//
//    @Value("${aws.ses.access-key-id}")
//    private String accessKey;
//
//    @Value("${aws.ses.secret-access-key}")
//    private String secretKey;
//
//    @Value("${aws.ses.region}")
//    private String region;
//
//    @Value("${aws.ses.sender}")
//    private String sender;
//
//    private SesClient buildClient() {
//        // If no real credentials yet, skip silently
//        if (accessKey == null || accessKey.isBlank() ||
//            secretKey == null || secretKey.isBlank()) {
//            return null;
//        }
//        return SesClient.builder()
//            .region(Region.of(region))
//            .credentialsProvider(StaticCredentialsProvider.create(
//                AwsBasicCredentials.create(accessKey, secretKey)))
//            .build();
//    }
//
//    public void sendWelcomeEmail(String toEmail, String userName) {
//        SesClient client = buildClient();
//
//        // Skip if credentials not set yet
//        if (client == null) {
//            System.out.println("[SES] Skipping welcome email — AWS credentials not configured yet.");
//            return;
//        }
//
//        String subject = "Welcome to ILM ORA 🎉";
//
//        String htmlBody = """
//            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
//                <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);
//                            padding:30px;border-radius:12px;text-align:center;">
//                    <h1 style="color:white;margin:0;font-size:28px;">ILM ORA</h1>
//                    <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">
//                        Learning Management System
//                    </p>
//                </div>
//
//                <div style="padding:30px;background:#f8fafc;
//                            border-radius:12px;margin-top:16px;
//                            border:1px solid #e2e8f0;">
//                    <h2 style="color:#1e293b;margin-top:0;">Hi %s 👋</h2>
//                    <p style="color:#475569;font-size:15px;line-height:1.7;">
//                        Thank you for joining <strong style="color:#6366f1;">ILM ORA</strong>!
//                        Your account has been successfully created and you are all set to go.
//                    </p>
//
//                    <div style="background:#fff;border-radius:10px;
//                                padding:20px;border:1px solid #e2e8f0;margin:20px 0;">
//                        <p style="color:#374151;font-size:14px;font-weight:700;
//                                  margin:0 0 12px;">
//                            🚀 Upgrade to Prime for more features:
//                        </p>
//                        <ul style="color:#475569;font-size:14px;
//                                   line-height:2;margin:0;padding-left:20px;">
//                            <li>✅ Unlimited courses and live sessions</li>
//                            <li>✅ Advanced analytics and progress reports</li>
//                            <li>✅ Priority customer support</li>
//                            <li>✅ Custom branding for your organization</li>
//                            <li>✅ Bulk student and trainer management</li>
//                        </ul>
//                    </div>
//
//                    <div style="text-align:center;margin-top:24px;">
//                        <a href="https://ilm.ora.texora.ai/pricing"
//                           style="background:linear-gradient(135deg,#6366f1,#8b5cf6);
//                                  color:white;padding:14px 36px;border-radius:9px;
//                                  text-decoration:none;font-weight:bold;
//                                  font-size:15px;display:inline-block;">
//                            Upgrade to Prime →
//                        </a>
//                    </div>
//
//                    <p style="color:#94a3b8;font-size:12px;
//                              margin-top:28px;text-align:center;line-height:1.6;">
//                        This is an automated message from ILM ORA.<br/>
//                        Please do not reply to this email.<br/>
//                        © 2026 ILM ORA. All rights reserved.
//                    </p>
//                </div>
//            </div>
//        """.formatted(userName != null ? userName : "there");
//
//        try {
//            SendEmailRequest request = SendEmailRequest.builder()
//                .destination(Destination.builder()
//                    .toAddresses(toEmail)
//                    .build())
//                .message(Message.builder()
//                    .subject(Content.builder()
//                        .data(subject)
//                        .charset("UTF-8")
//                        .build())
//                    .body(Body.builder()
//                        .html(Content.builder()
//                            .data(htmlBody)
//                            .charset("UTF-8")
//                            .build())
//                        .build())
//                    .build())
//                .source(sender)
//                .build();
//
//            client.sendEmail(request);
//            System.out.println("[SES] Welcome email sent to: " + toEmail);
//
//        } catch (Exception e) {
//            // Never crash the signup flow because of email failure
//            System.err.println("[SES] Failed to send welcome email to "
//                + toEmail + ": " + e.getMessage());
//        } finally {
//            client.close();
//        }
//    }
//}



package com.lms.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class SesEmailService {

    @Value("${aws.ses.access-key-id}")
    private String accessKey;

    @Value("${aws.ses.secret-access-key}")
    private String secretKey;

    @Value("${aws.ses.region}")
    private String region;

    @Value("${aws.ses.sender}")
    private String sender;

    private SesClient buildClient() {
        if (accessKey == null || accessKey.isBlank() ||
            secretKey == null || secretKey.isBlank()) {
            return null;
        }
        return SesClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  NEW ▸ Sent when admin creates a user manually.
    //  User gets their email + a link to set their own password.
    //  Welcome email fires separately after they complete the reset.
    // ═══════════════════════════════════════════════════════════════════════
    public void sendSetPasswordEmail(String toEmail, String userName, String setPasswordLink) {
        SesClient client = buildClient();
        if (client == null) {
            System.out.println("[SES] Skipping set-password email — AWS credentials not configured yet.");
            return;
        }

        String subject = "You've been added to ILM ORA — Set your password";

        String htmlBody = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
                <div style="background:linear-gradient(135deg,#f97316,#ea580c);
                            padding:30px;border-radius:12px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:28px;">ILM ORA</h1>
                    <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">
                        Learning Management System
                    </p>
                </div>

                <div style="padding:30px;background:#f8fafc;
                            border-radius:12px;margin-top:16px;
                            border:1px solid #e2e8f0;">
                    <h2 style="color:#1e293b;margin-top:0;">Hi %s 👋</h2>
                    <p style="color:#475569;font-size:15px;line-height:1.7;">
                        Your account on <strong style="color:#f97316;">ILM ORA</strong> has been created
                        by your organization admin. Before you can log in, you need to set your password.
                    </p>

                    <div style="background:#fff;border-radius:10px;
                                padding:20px;border:1px solid #e2e8f0;margin:20px 0;">
                        <p style="color:#374151;font-size:14px;font-weight:700;margin:0 0 4px;">
                            Your login email
                        </p>
                        <p style="color:#6366f1;font-size:15px;font-weight:600;margin:0;">
                            %s
                        </p>
                    </div>

                    <div style="text-align:center;margin-top:24px;">
                        <a href="%s"
                           style="background:linear-gradient(135deg,#f97316,#ea580c);
                                  color:white;padding:14px 36px;border-radius:9px;
                                  text-decoration:none;font-weight:bold;
                                  font-size:15px;display:inline-block;">
                            Set My Password →
                        </a>
                    </div>

                    <p style="color:#94a3b8;font-size:12px;margin-top:20px;text-align:center;line-height:1.6;">
                        This link expires in <strong>15 minutes</strong>. If you did not expect this
                        email, you can safely ignore it.<br/>
                        © 2026 ILM ORA. All rights reserved.
                    </p>
                </div>
            </div>
        """.formatted(
            userName != null ? userName : "there",
            toEmail,
            setPasswordLink
        );

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).charset("UTF-8").build())
                    .body(Body.builder()
                        .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                        .build())
                    .build())
                .source(sender)
                .build();

            client.sendEmail(request);
            System.out.println("[SES] Set-password email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("[SES] Failed to send set-password email to "
                + toEmail + ": " + e.getMessage());
        } finally {
            client.close();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EXISTING ▸ Sent after the user successfully sets / resets their password
    //  (moved from register() — now called from resetPassword()).
    // ═══════════════════════════════════════════════════════════════════════
    public void sendWelcomeEmail(String toEmail, String userName) {
        SesClient client = buildClient();
        if (client == null) {
            System.out.println("[SES] Skipping welcome email — AWS credentials not configured yet.");
            return;
        }

        String subject = "Welcome to ILM ORA 🎉";

        String htmlBody = """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;">
                <div style="background:linear-gradient(135deg,#6366f1,#8b5cf6);
                            padding:30px;border-radius:12px;text-align:center;">
                    <h1 style="color:white;margin:0;font-size:28px;">ILM ORA</h1>
                    <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">
                        Learning Management System
                    </p>
                </div>

                <div style="padding:30px;background:#f8fafc;
                            border-radius:12px;margin-top:16px;
                            border:1px solid #e2e8f0;">
                    <h2 style="color:#1e293b;margin-top:0;">Hi %s 👋</h2>
                    <p style="color:#475569;font-size:15px;line-height:1.7;">
                        Thank you for joining <strong style="color:#6366f1;">ILM ORA</strong>!
                        Your account is all set and ready to go.
                    </p>

                    <div style="background:#fff;border-radius:10px;
                                padding:20px;border:1px solid #e2e8f0;margin:20px 0;">
                        <p style="color:#374151;font-size:14px;font-weight:700;
                                  margin:0 0 12px;">
                            🚀 Upgrade to Prime for more features:
                        </p>
                        <ul style="color:#475569;font-size:14px;
                                   line-height:2;margin:0;padding-left:20px;">
                            <li>✅ Unlimited courses and live sessions</li>
                            <li>✅ Advanced analytics and progress reports</li>
                            <li>✅ Priority customer support</li>
                            <li>✅ Custom branding for your organization</li>
                            <li>✅ Bulk student and trainer management</li>
                        </ul>
                    </div>

                    <div style="text-align:center;margin-top:24px;">
                        <a href="https://ilmora.texora.ai/pricing"
                           style="background:linear-gradient(135deg,#6366f1,#8b5cf6);
                                  color:white;padding:14px 36px;border-radius:9px;
                                  text-decoration:none;font-weight:bold;
                                  font-size:15px;display:inline-block;">
                            Upgrade to Prime →
                        </a>
                    </div>

                    <p style="color:#94a3b8;font-size:12px;
                              margin-top:28px;text-align:center;line-height:1.6;">
                        This is an automated message from ILM ORA.<br/>
                        Please do not reply to this email.<br/>
                        © 2026 ILM ORA. All rights reserved.
                    </p>
                </div>
            </div>
        """.formatted(userName != null ? userName : "there");

        try {
            SendEmailRequest request = SendEmailRequest.builder()
                .destination(Destination.builder().toAddresses(toEmail).build())
                .message(Message.builder()
                    .subject(Content.builder().data(subject).charset("UTF-8").build())
                    .body(Body.builder()
                        .html(Content.builder().data(htmlBody).charset("UTF-8").build())
                        .build())
                    .build())
                .source(sender)
                .build();

            client.sendEmail(request);
            System.out.println("[SES] Welcome email sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("[SES] Failed to send welcome email to "
                + toEmail + ": " + e.getMessage());
        } finally {
            client.close();
        }
    }
}
