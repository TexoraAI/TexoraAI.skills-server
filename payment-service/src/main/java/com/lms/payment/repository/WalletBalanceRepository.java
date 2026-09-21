package com.lms.payment.repository;

import com.lms.payment.entity.WalletBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, UUID> {

    Optional<WalletBalance> findByUserId(UUID userId);

    // Pessimistic write lock used inside a @Transactional method to serialize
    // concurrent credit/deduct operations on the same user's wallet row.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from WalletBalance w where w.userId = :userId")
    Optional<WalletBalance> findByUserIdForUpdate(@Param("userId") UUID userId);
}
