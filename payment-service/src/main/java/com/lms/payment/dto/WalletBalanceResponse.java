package com.lms.payment.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class WalletBalanceResponse {

    private UUID userId;
    private Integer balance;
    private LocalDateTime updatedAt;

    public WalletBalanceResponse() {
    }

    public WalletBalanceResponse(UUID userId, Integer balance, LocalDateTime updatedAt) {
        this.userId = userId;
        this.balance = balance;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
