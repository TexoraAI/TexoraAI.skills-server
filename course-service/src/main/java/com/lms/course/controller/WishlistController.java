package com.lms.course.controller;

import com.lms.course.dto.WishlistToggleResponseDTO;
import com.lms.course.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // POST /api/course/v1/wishlist/toggle/{programId}
    @PostMapping("/toggle/{programId}")
    public ResponseEntity<WishlistToggleResponseDTO> toggle(
            @PathVariable Long programId,
            Authentication authentication) {

        String userEmail = authentication.getName(); // set by JwtFilter → UsernamePasswordAuthenticationToken(email, ...)
        return ResponseEntity.ok(wishlistService.toggle(programId, userEmail, userEmail));
    }

    // GET /api/course/v1/wishlist/my  → just the ids, used to hydrate hearts on load
    @GetMapping("/my")
    public ResponseEntity<List<Long>> getMyWishlist(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(wishlistService.getMyWishlistedProgramIds(userEmail));
    }
}