package com.lms.live_session.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.live_session.dto.EmailRequestDTO;
import com.lms.live_session.dto.EmailResponseDTO;
import com.lms.live_session.dto.EmailStatsDTO;
import com.lms.live_session.entity.Email;
import com.lms.live_session.entity.EmailStatus;
import com.lms.live_session.event.ComposedEmailEvent;
import com.lms.live_session.kafka.NotificationProducer;
import com.lms.live_session.repository.EmailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmailService {

    private final EmailRepository emailRepository;
    private final com.lms.live_session.repository.EmailAttachmentRepository attachmentRepository;
    private final NotificationProducer notificationProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String UPLOAD_DIR = "uploads/email-attachments";

    @Autowired
    public EmailService(EmailRepository emailRepository,
                         com.lms.live_session.repository.EmailAttachmentRepository attachmentRepository,
                         NotificationProducer notificationProducer) {
        this.emailRepository = emailRepository;
        this.attachmentRepository = attachmentRepository;
        this.notificationProducer = notificationProducer;
    }

    public EmailResponseDTO draftEmail(EmailRequestDTO dto, String creatorId, String fromEmail,
            List<org.springframework.web.multipart.MultipartFile> files) {
validate(dto);

Email email = new Email();
email.setSubject(dto.getSubject());
email.setBody(dto.getBody());
email.setFromEmail(fromEmail);
email.setToEmails(serializeEmailList(dto.getToEmails()));
email.setCcEmails(serializeEmailList(dto.getCcEmails()));
email.setBccEmails(serializeEmailList(dto.getBccEmails()));
email.setStatus(EmailStatus.DRAFT);
email.setCreatorId(creatorId);

Email saved = emailRepository.save(email);
saveAttachments(saved.getId(), files);
return mapToDTO(saved);
}

    public EmailResponseDTO sendEmail(EmailRequestDTO dto, String creatorId, String fromEmail,
            List<org.springframework.web.multipart.MultipartFile> files) {
validate(dto);

        Email email = new Email();
        email.setSubject(dto.getSubject());
        email.setBody(dto.getBody());
        email.setFromEmail(fromEmail);
        email.setToEmails(serializeEmailList(dto.getToEmails()));
        email.setCcEmails(serializeEmailList(dto.getCcEmails()));
        email.setBccEmails(serializeEmailList(dto.getBccEmails()));
        email.setCreatorId(creatorId);

        // Delivery itself happens asynchronously downstream in
        // notification-service. SENT here means "handed off to the
        // notification pipeline", not "confirmed delivered" — there's no
        // synchronous send result to check against.
        try {
            ComposedEmailEvent event = new ComposedEmailEvent(
                    null, // emailId filled in after save, below
                    fromEmail,
                    dto.getToEmails(),
                    dto.getCcEmails(),
                    dto.getBccEmails(),
                    dto.getSubject(),
                    dto.getBody(),
                    creatorId
            );

            email.setStatus(EmailStatus.SENT);
            email.setSentAt(LocalDateTime.now());
            Email saved = emailRepository.save(email);
            saveAttachments(saved.getId(), files);

            event.setEmailId(saved.getId());
            notificationProducer.sendComposedEmail(event);

            return mapToDTO(saved);
        } catch (Exception e) {
            email.setStatus(EmailStatus.FAILED);
            Email saved = emailRepository.save(email);
            System.err.println("Failed to publish composed email event for emailId=" + saved.getId()
                    + ": " + e.getMessage());
            return mapToDTO(saved);
        }
    }

    @Transactional(readOnly = true)
    public List<EmailResponseDTO> getMyEmails(String creatorId) {
        return emailRepository.findByCreatorIdOrderByCreatedAtDesc(creatorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EmailResponseDTO> getEmailsByStatus(String creatorId, String status) {
        EmailStatus emailStatus = parseStatus(status);
        return emailRepository.findByCreatorIdAndStatusOrderByCreatedAtDesc(creatorId, emailStatus).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EmailResponseDTO getEmailById(Long emailId, String creatorId) {
        Email email = emailRepository.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        verifyOwnership(email, creatorId);

        return mapToDTO(email);
    }

    public EmailResponseDTO updateDraft(Long emailId, EmailRequestDTO dto, String creatorId) {
        Email email = emailRepository.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        verifyOwnership(email, creatorId);

        if (email.getStatus() != EmailStatus.DRAFT) {
            throw new IllegalArgumentException("Only draft emails can be updated");
        }

        validate(dto);

        email.setSubject(dto.getSubject());
        email.setBody(dto.getBody());
        email.setToEmails(serializeEmailList(dto.getToEmails()));
        email.setCcEmails(serializeEmailList(dto.getCcEmails()));
        email.setBccEmails(serializeEmailList(dto.getBccEmails()));

        Email saved = emailRepository.save(email);
        return mapToDTO(saved);
    }

    public void deleteEmail(Long emailId, String creatorId) {
        Email email = emailRepository.findById(emailId)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        verifyOwnership(email, creatorId);

        emailRepository.delete(email);
    }

    /**
     * ASSUMPTION: this system has no "read/unread" concept for sent mail —
     * there's no inbox here, just DRAFT/SENT/FAILED records the creator
     * authored. "unread" is treated as FAILED emails needing the user's
     * attention. If you actually want inbox-style unread tracking, that
     * needs a separate received-mail model — let me know and I'll add it.
     */
    @Transactional(readOnly = true)
    public EmailStatsDTO getEmailStats(String creatorId) {
        long sent = emailRepository.countByCreatorIdAndStatus(creatorId, EmailStatus.SENT);
        long drafts = emailRepository.countByCreatorIdAndStatus(creatorId, EmailStatus.DRAFT);
        long unread = emailRepository.countByCreatorIdAndStatus(creatorId, EmailStatus.FAILED);

        return new EmailStatsDTO((int) unread, (int) sent, (int) drafts);
    }

    private void validate(EmailRequestDTO dto) {
        if (!StringUtils.hasText(dto.getSubject())) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (!StringUtils.hasText(dto.getBody())) {
            throw new IllegalArgumentException("Body is required");
        }
        if (CollectionUtils.isEmpty(dto.getToEmails())) {
            throw new IllegalArgumentException("At least one recipient (toEmails) is required");
        }
    }

    private EmailStatus parseStatus(String status) {
        try {
            return EmailStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private void verifyOwnership(Email email, String creatorId) {
        if (!email.getCreatorId().equals(creatorId)) {
            throw new IllegalArgumentException("You do not have permission to access this email");
        }
    }

    private EmailResponseDTO mapToDTO(Email email) {
        return new EmailResponseDTO(
                email.getId(),
                email.getSubject(),
                email.getBody(),
                email.getFromEmail(),
                parseEmailList(email.getToEmails()),
                parseEmailList(email.getCcEmails()),
                parseEmailList(email.getBccEmails()),
                email.getStatus() == null ? null : email.getStatus().name(),
                email.getCreatorId(),
                email.getCreatedAt(),
                email.getSentAt()
        );
    }

    private List<String> parseEmailList(String jsonString) {
        if (!StringUtils.hasText(jsonString)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String serializeEmailList(List<String> emails) {
        if (CollectionUtils.isEmpty(emails)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(emails);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize email list: " + e.getMessage());
        }
    }
    private void saveAttachments(Long emailId, List<org.springframework.web.multipart.MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        java.io.File dir = new java.io.File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        for (org.springframework.web.multipart.MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            try {
                String safeName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                java.io.File dest = new java.io.File(dir, safeName);
                file.transferTo(dest);

                com.lms.live_session.entity.EmailAttachment attachment = new com.lms.live_session.entity.EmailAttachment();
                attachment.setEmailId(emailId);
                attachment.setFileName(file.getOriginalFilename());
                attachment.setContentType(file.getContentType());
                attachment.setFileSize(file.getSize());
                attachment.setStoragePath(dest.getPath());
                attachmentRepository.save(attachment);
            } catch (Exception e) {
                System.err.println("Failed to save attachment for emailId=" + emailId + ": " + e.getMessage());
            }
        }
    }
}