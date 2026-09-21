package com.lms.payment.dto;

import com.lms.payment.entity.PaymentPurpose;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public class InitiatePaymentRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    private UUID orgId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private Integer amount;

    @NotNull(message = "purpose is required")
    private PaymentPurpose purpose;

    @NotNull(message = "referenceId is required")
    private String referenceId;

    public InitiatePaymentRequest() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public UUID getOrgId() {
        return orgId;
    }

    public void setOrgId(UUID orgId) {
        this.orgId = orgId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public PaymentPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(PaymentPurpose purpose) {
        this.purpose = purpose;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }
}