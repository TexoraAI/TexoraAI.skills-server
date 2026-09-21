package com.lms.payment.repository;

import com.lms.payment.entity.Payment;
import com.lms.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByOrderId(String orderId);

    // Used by PaymentService.reconcilePendingPayments() to find stuck CREATED
    // payments older than the configured threshold.
    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime cutoff);
}
