package com.lms.payment.exception;

// Thrown when RazorpayClient can't complete a call — connect/read timeout,
// connection refused, DNS failure, or a non-2xx response from Razorpay.
// Kept distinct from PaymentNotFoundException etc. so GlobalExceptionHandler
// can return a 503 (retry-able, not our bug) instead of a generic 500.
public class PaymentGatewayException extends RuntimeException {

    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}