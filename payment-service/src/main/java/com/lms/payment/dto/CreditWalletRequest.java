package com.lms.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public class CreditWalletRequest {

    @NotNull(message = "userId is required")
    private UUID userId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private Integer amount;

    @NotBlank(message = "reason is required")
    private String reason;

    @NotBlank(message = "idempotencyKey is required")
    private String idempotencyKey;

    public CreditWalletRequest() {
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}
