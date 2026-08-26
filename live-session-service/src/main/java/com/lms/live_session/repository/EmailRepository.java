package com.lms.live_session.repository;

import com.lms.live_session.entity.Email;
import com.lms.live_session.entity.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Note: status is mapped as an EmailStatus enum on the Email entity (via
 * @Enumerated(EnumType.STRING)), so these derived-query parameters use
 * EmailStatus rather than raw String — same reasoning as ReminderRepository.
 */
public interface EmailRepository extends JpaRepository<Email, Long> {

    List<Email> findByCreatorIdAndStatusOrderByCreatedAtDesc(String creatorId, EmailStatus status);

    List<Email> findByCreatorIdOrderByCreatedAtDesc(String creatorId);

    List<Email> findByStatus(EmailStatus status);

    long countByCreatorIdAndStatus(String creatorId, EmailStatus status);

    void deleteByCreatorId(String creatorId);
}