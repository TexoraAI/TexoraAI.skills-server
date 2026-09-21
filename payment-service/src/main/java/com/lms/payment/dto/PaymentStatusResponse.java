package com.lms.payment.dto;

import com.lms.payment.entity.PaymentPurpose;
import com.lms.payment.entity.PaymentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentStatusResponse {

    private UUID paymentId;
    private String orderId;
    private String razorpayPaymentId;
    private Integer amount;
    private PaymentStatus status;
    private PaymentPurpose purpose;
    private LocalDateTime updatedAt;

    public PaymentStatusResponse() {
    }

    public PaymentStatusResponse(UUID paymentId, String orderId, String razorpayPaymentId, Integer amount,
                                  PaymentStatus status, PaymentPurpose purpose, LocalDateTime updatedAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.amount = amount;
        this.status = status;
        this.purpose = purpose;
        this.updatedAt = updatedAt;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public PaymentPurpose getPurpose() {
        return purpose;
    }

    public void setPurpose(PaymentPurpose purpose) {
        this.purpose = purpose;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
