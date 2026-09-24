package com.lms.live_session.repository;

import com.lms.live_session.entity.TexoraWebhookOutbox;
import com.lms.live_session.entity.WebhookOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TexoraWebhookOutboxRepository extends JpaRepository<TexoraWebhookOutbox, Long> {
    List<TexoraWebhookOutbox> findByStatusAndNextAttemptAtLessThanEqual(WebhookOutboxStatus status, LocalDateTime now);
}