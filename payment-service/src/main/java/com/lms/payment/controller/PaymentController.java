package com.lms.payment.controller;

import com.lms.payment.dto.InitiatePaymentRequest;
import com.lms.payment.dto.InitiatePaymentResponse;
import com.lms.payment.dto.PaymentStatusResponse;
import com.lms.payment.security.AuthenticatedUser;
import com.lms.payment.security.CurrentUser;
import com.lms.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<InitiatePaymentResponse> initiate(
            @Valid @RequestBody InitiatePaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

               AuthenticatedUser caller = CurrentUser.get();

        // Org scoping ONLY applies when this is an org-scoped purchase
        // (request.getOrgId() != null — i.e. an org-plan upgrade). Individual
        // and resume-specific plan purchases legitimately send orgId=null,
        // and must NOT be rejected just because the caller (or a standalone
        // caller) has no organizationId.
        if (request.getOrgId() != null) {
            UUID callerOrgId = caller.getOrganizationId();
            if (callerOrgId == null || !Objects.equals(callerOrgId, request.getOrgId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Still applies to every purchase type: a caller can only pay for
        // themselves, unless they're a TENANT_ADMIN acting on someone else's
        // behalf (e.g. an org admin buying a seat upgrade for the org).
        if (!"TENANT_ADMIN".equalsIgnoreCase(caller.getRole())
                && !Objects.equals(caller.getUserId(), request.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        InitiatePaymentResponse response = paymentService.initiatePayment(request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    // Razorpay signs the raw request body, so it must be read as a plain String
    // (not deserialized straight to a DTO) before signature verification happens
    // inside PaymentService.handleWebhook().
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<Void> webhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        paymentService.handleWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<PaymentStatusResponse> getStatus(@PathVariable String orderId) {
        return ResponseEntity.ok(paymentService.getStatusByOrderId(orderId));
    }
}
