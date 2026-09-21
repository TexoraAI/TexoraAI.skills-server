package com.lms.payment.service;

import com.lms.payment.entity.WalletBalance;
import com.lms.payment.repository.WalletBalanceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

// Split into its own Spring bean (not a private method on WalletService) so the
// @Transactional(REQUIRES_NEW) below actually goes through Spring's proxy —
// self-invocation from within the same class silently ignores @Transactional.
//
// Purpose: findByUserIdForUpdate's SELECT ... FOR UPDATE only locks a row that
// already exists. For a brand-new user's very first credit/deduct, there's no
// row to lock, so two concurrent first-time calls can both decide "no row,
// let's insert one" and collide on the userId primary key. Running the insert
// attempt in its own transaction means a failed insert only rolls back this
// tiny transaction — not the caller's wallet-update transaction — so the
// caller can safely re-query afterward and always find a row to lock.
@Component
public class WalletBalanceInitializer {

    private final WalletBalanceRepository walletBalanceRepository;

    public WalletBalanceInitializer(WalletBalanceRepository walletBalanceRepository) {
        this.walletBalanceRepository = walletBalanceRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureExists(UUID userId) {
        if (walletBalanceRepository.existsById(userId)) {
            return;
        }
        try {
            WalletBalance fresh = new WalletBalance();
            fresh.setUserId(userId);
            fresh.setBalance(0);
            walletBalanceRepository.saveAndFlush(fresh);
        } catch (DataIntegrityViolationException ex) {
            // Lost the race — another concurrent first-credit/deduct already inserted
            // this user's row a moment earlier. That's fine: the row exists now,
            // which is all the caller needs.
        }
    }
}