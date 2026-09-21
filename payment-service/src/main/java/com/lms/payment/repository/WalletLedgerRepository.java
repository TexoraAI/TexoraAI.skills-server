package com.lms.payment.repository;

import com.lms.payment.entity.WalletLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface WalletLedgerRepository extends JpaRepository<WalletLedger, UUID> {

    Optional<WalletLedger> findByIdempotencyKey(String idempotencyKey);
}
