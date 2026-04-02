//package com.lms.auth.service;
//
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    public EmailService(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    public void sendResetPasswordMail(String toEmail, String resetLink) {
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Reset your LMS password");
//        message.setText(
//            "Click the link below to reset your password:\n\n" +
//            resetLink + "\n\n" +
//            "If you did not request this, please ignore."
//        );
//
//        mailSender.send(message);
//    }
//
//    // ✅ NEW: Email Verification Mail
//    public void sendVerificationMail(String toEmail, String verifyLink) {
//
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Verify your Email - TexoraAI.skills");
//        message.setText(
//            "Welcome to TexoraAI.skills!\n\n" +
//            "Please verify your email using the link below:\n\n" +
//            verifyLink + "\n\n" +
//            "If you did not create this account, please ignore."
//        );
//
//        mailSender.send(message);
//    }
//}



package com.lms.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordMail(String toEmail, String resetLink) {

        System.out.println("📩 Sending RESET PASSWORD mail to: " + toEmail);
        System.out.println("🔗 Reset Link: " + resetLink);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset your LMS password");
        message.setText(
                "Click the link below to reset your password:\n\n" +
                        resetLink + "\n\n" +
                        "If you did not request this, please ignore."
        );

        mailSender.send(message);

        System.out.println("✅ Reset Password mail SENT successfully to: " + toEmail);
    }

    public void sendVerificationMail(String toEmail, String verifyLink) {

        System.out.println("📩 Sending VERIFY EMAIL mail to: " + toEmail);
        System.out.println("🔗 Verify Link: " + verifyLink);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify your Email - TexoraAI.skills");
        message.setText(
                "Welcome to TexoraAI.skills!\n\n" +
                        "Please verify your email using the link below:\n\n" +
                        verifyLink + "\n\n" +
                        "If you did not create this account, please ignore."
        );

        mailSender.send(message);

        System.out.println("✅ Verification mail SENT successfully to: " + toEmail);
    }
    
}
