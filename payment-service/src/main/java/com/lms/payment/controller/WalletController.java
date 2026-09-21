package com.lms.payment.controller;

import com.lms.payment.dto.CreditWalletRequest;
import com.lms.payment.dto.DeductWalletRequest;
import com.lms.payment.dto.WalletBalanceResponse;
import com.lms.payment.dto.WalletLedgerResponse;
import com.lms.payment.security.AuthenticatedUser;
import com.lms.payment.security.CurrentUser;
import com.lms.payment.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/credit")
    public ResponseEntity<WalletLedgerResponse> credit(@Valid @RequestBody CreditWalletRequest request) {
        if (!isSelfOrTenantAdmin(request.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(walletService.creditWallet(request));
    }

    @PostMapping("/deduct")
    public ResponseEntity<WalletLedgerResponse> deduct(@Valid @RequestBody DeductWalletRequest request) {
        if (!isSelfOrTenantAdmin(request.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(walletService.deductWallet(request));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(@PathVariable UUID userId) {
        if (!isSelfOrTenantAdmin(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    // student/trainer can only touch their own wallet; tenant_admin can act on
    // behalf of any user (WalletBalance currently has no orgId column to scope
    // tenant_admin further — add one if cross-tenant admin access ever matters).
    private boolean isSelfOrTenantAdmin(UUID targetUserId) {
        AuthenticatedUser caller = CurrentUser.get();
        if (caller == null) {
            return false;
        }
        if ("TENANT_ADMIN".equalsIgnoreCase(caller.getRole())) {
            return true;
        }
        return Objects.equals(caller.getUserId(), targetUserId);
    }
}
