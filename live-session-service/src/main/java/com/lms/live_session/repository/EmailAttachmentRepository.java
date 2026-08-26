package com.lms.live_session.repository;

import com.lms.live_session.entity.EmailAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmailAttachmentRepository extends JpaRepository<EmailAttachment, Long> {
    List<EmailAttachment> findByEmailId(Long emailId);
}