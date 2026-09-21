package com.lms.payment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class WalletLedgerResponse {

    private UUID id;
    private UUID userId;
    private Integer amount;
    private String reason;
    private Integer balanceAfter;
    private LocalDateTime createdAt;

    public WalletLedgerResponse() {
    }

    public WalletLedgerResponse(UUID id, UUID userId, Integer amount, String reason,
                                 Integer balanceAfter, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.reason = reason;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
