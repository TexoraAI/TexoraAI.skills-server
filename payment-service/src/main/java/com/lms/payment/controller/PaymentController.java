package com.lms.payment.controller;

import com.lms.payment.model.Payment;
import com.lms.payment.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments") // ✅ PLURAL
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }
//hfjffvhhvjhj
    // 1️⃣ CREATE PAYMENT
    @PostMapping
    public ResponseEntity<Payment> makePayment(@RequestBody Payment req) {
        return ResponseEntity.ok(service.createPayment(req));
    }

    // 2️⃣ GET PAYMENT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Payment> get(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3️⃣ GET PAYMENTS BY USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Payment>> listByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                service.listPaymentsForUser(userId, PageRequest.of(page, size))
        );
    }

    // 4️⃣ UPDATE PAYMENT STATUS  ✅ (YOU WERE CALLING THIS)
    @PutMapping("/{id}/status")
    public ResponseEntity<Payment> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return ResponseEntity.ok(
                service.updateStatus(id, body.get("status"))
        );
    }
}
