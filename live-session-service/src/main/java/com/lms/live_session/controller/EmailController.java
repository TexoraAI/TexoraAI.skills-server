package com.lms.live_session.controller;

import com.lms.live_session.dto.EmailRequestDTO;
import com.lms.live_session.dto.EmailResponseDTO;
import com.lms.live_session.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }


    @PostMapping(value = "/draft", consumes = "multipart/form-data")
    public ResponseEntity<?> draftEmail(
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam(value = "toEmails", required = false) List<String> toEmails,
            @RequestParam(value = "ccEmails", required = false) List<String> ccEmails,
            @RequestParam(value = "bccEmails", required = false) List<String> bccEmails,
            @RequestParam(value = "files", required = false) List<org.springframework.web.multipart.MultipartFile> files,
            Authentication auth) {
        try {
            EmailRequestDTO dto = new EmailRequestDTO(subject, body, toEmails, ccEmails, bccEmails);
            return ResponseEntity.ok(emailService.draftEmail(dto, auth.getName(), auth.getName(), files));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping(value = "/send", consumes = "multipart/form-data")
    public ResponseEntity<?> sendEmail(
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            @RequestParam(value = "toEmails", required = false) List<String> toEmails,
            @RequestParam(value = "ccEmails", required = false) List<String> ccEmails,
            @RequestParam(value = "bccEmails", required = false) List<String> bccEmails,
            @RequestParam(value = "files", required = false) List<org.springframework.web.multipart.MultipartFile> files,
            Authentication auth) {
        try {
            EmailRequestDTO dto = new EmailRequestDTO(subject, body, toEmails, ccEmails, bccEmails);
            return ResponseEntity.ok(emailService.sendEmail(dto, auth.getName(), auth.getName(), files));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<EmailResponseDTO>> getMyEmails(@RequestParam(required = false) String status, Authentication auth) {
        try {
            if (status != null) {
                return ResponseEntity.ok(emailService.getEmailsByStatus(auth.getName(), status));
            }
            return ResponseEntity.ok(emailService.getMyEmails(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmailById(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(emailService.getEmailById(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDraft(@PathVariable Long id, @RequestBody EmailRequestDTO dto, Authentication auth) {
        try {
            return ResponseEntity.ok(emailService.updateDraft(id, dto, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmail(@PathVariable Long id, Authentication auth) {
        try {
            emailService.deleteEmail(id, auth.getName());
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getEmailStats(Authentication auth) {
        try {
            return ResponseEntity.ok(emailService.getEmailStats(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}