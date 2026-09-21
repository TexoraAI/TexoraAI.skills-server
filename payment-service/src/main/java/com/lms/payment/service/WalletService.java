package com.lms.payment.service;

import com.lms.payment.dto.CreditWalletRequest;
import com.lms.payment.dto.DeductWalletRequest;
import com.lms.payment.dto.WalletBalanceResponse;
import com.lms.payment.dto.WalletLedgerResponse;
import com.lms.payment.entity.WalletBalance;
import com.lms.payment.entity.WalletLedger;
import com.lms.payment.exception.InsufficientBalanceException;
import com.lms.payment.exception.InvalidIdempotencyKeyException;
import com.lms.payment.repository.WalletBalanceRepository;
import com.lms.payment.repository.WalletLedgerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

// Single concrete service class (no interface / impl split) per the module's constraints.
@Service
public class WalletService {

    private final WalletBalanceRepository walletBalanceRepository;
    private final WalletLedgerRepository walletLedgerRepository;
    private final WalletBalanceInitializer walletBalanceInitializer;

    public WalletService(WalletBalanceRepository walletBalanceRepository,
                          WalletLedgerRepository walletLedgerRepository,
                          WalletBalanceInitializer walletBalanceInitializer) {
        this.walletBalanceRepository = walletBalanceRepository;
        this.walletLedgerRepository = walletLedgerRepository;
        this.walletBalanceInitializer = walletBalanceInitializer;
    }

    @Transactional
    public WalletLedgerResponse creditWallet(CreditWalletRequest dto) {
        if (!StringUtils.hasText(dto.getIdempotencyKey())) {
            throw new InvalidIdempotencyKeyException("idempotencyKey is required");
        }

        Optional<WalletLedger> existing = walletLedgerRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if (existing.isPresent()) {
            return toLedgerResponse(existing.get());
        }

        // Guarantees a row exists for this userId (safe under concurrent first-credits)
        // before we try to lock it below.
        walletBalanceInitializer.ensureExists(dto.getUserId());

        WalletBalance balance = walletBalanceRepository.findByUserIdForUpdate(dto.getUserId())
                .orElseThrow(() -> new IllegalStateException(
                        "WalletBalance row missing after ensureExists for userId: " + dto.getUserId()));
        int newBalance = balance.getBalance() + dto.getAmount();
        balance.setBalance(newBalance);
        walletBalanceRepository.saveAndFlush(balance);

        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(dto.getUserId());
        ledger.setAmount(dto.getAmount());
        ledger.setReason(dto.getReason());
        ledger.setIdempotencyKey(dto.getIdempotencyKey());
        ledger.setBalanceAfter(newBalance);
        try {
            ledger = walletLedgerRepository.saveAndFlush(ledger);
        } catch (DataIntegrityViolationException ex) {
            return walletLedgerRepository.findByIdempotencyKey(dto.getIdempotencyKey())
                    .map(this::toLedgerResponse)
                    .orElseThrow(() -> ex);
        }

        return toLedgerResponse(ledger);
    }

    @Transactional
    public WalletLedgerResponse deductWallet(DeductWalletRequest dto) {
        if (!StringUtils.hasText(dto.getIdempotencyKey())) {
            throw new InvalidIdempotencyKeyException("idempotencyKey is required");
        }

        Optional<WalletLedger> existing = walletLedgerRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if (existing.isPresent()) {
            return toLedgerResponse(existing.get());
        }

        // Guarantees a row exists for this userId (safe under concurrent first-deducts)
        // before we try to lock it below.
        walletBalanceInitializer.ensureExists(dto.getUserId());

        WalletBalance balance = walletBalanceRepository.findByUserIdForUpdate(dto.getUserId())
                .orElseThrow(() -> new IllegalStateException(
                        "WalletBalance row missing after ensureExists for userId: " + dto.getUserId()));

        if (balance.getBalance() < dto.getAmount()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance for userId: " + dto.getUserId());
        }

        int newBalance = balance.getBalance() - dto.getAmount();
        balance.setBalance(newBalance);
        walletBalanceRepository.saveAndFlush(balance);

        WalletLedger ledger = new WalletLedger();
        ledger.setUserId(dto.getUserId());
        ledger.setAmount(-dto.getAmount());
        ledger.setReason(dto.getReason());
        ledger.setIdempotencyKey(dto.getIdempotencyKey());
        ledger.setBalanceAfter(newBalance);
        try {
            ledger = walletLedgerRepository.saveAndFlush(ledger);
        } catch (DataIntegrityViolationException ex) {
            return walletLedgerRepository.findByIdempotencyKey(dto.getIdempotencyKey())
                    .map(this::toLedgerResponse)
                    .orElseThrow(() -> ex);
        }

        return toLedgerResponse(ledger);
    }

    public WalletBalanceResponse getBalance(UUID userId) {
        return walletBalanceRepository.findByUserId(userId)
                .map(b -> new WalletBalanceResponse(b.getUserId(), b.getBalance(), b.getUpdatedAt()))
                .orElseGet(() -> new WalletBalanceResponse(userId, 0, LocalDateTime.now()));
    }

    private WalletLedgerResponse toLedgerResponse(WalletLedger ledger) {
        return new WalletLedgerResponse(
                ledger.getId(),
                ledger.getUserId(),
                ledger.getAmount(),
                ledger.getReason(),
                ledger.getBalanceAfter(),
                ledger.getCreatedAt()
        );
    }
}