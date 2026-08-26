package com.lms.chat.controller;
import com.lms.chat.dto.ChatFeatureFlagsDTO;
import com.lms.chat.security.JwtUtil;
import com.lms.chat.service.ChatFeatureFlagsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat-feature-flags")
public class ChatFeatureFlagsController {
    private final ChatFeatureFlagsService featureFlagsService;
    private final JwtUtil jwtUtil;

    public ChatFeatureFlagsController(ChatFeatureFlagsService featureFlagsService,
                                       JwtUtil jwtUtil) {
        this.featureFlagsService = featureFlagsService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/org/{organizationId}")
    public ResponseEntity<ChatFeatureFlagsDTO> getOrgFlags(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(featureFlagsService.getOrgFlags(organizationId));
    }
    @PutMapping("/org/{organizationId}")
    public ResponseEntity<ChatFeatureFlagsDTO> updateOrgFlags(
            @PathVariable String organizationId,
            @RequestBody ChatFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateOrgFlags(organizationId, dto));
    }
    @GetMapping("/individual")
    public ResponseEntity<ChatFeatureFlagsDTO> getIndividualFlags(
            @RequestParam String email) {
        return ResponseEntity.ok(featureFlagsService.getIndividualFlags(email));
    }
    @PutMapping("/individual")
    public ResponseEntity<ChatFeatureFlagsDTO> updateIndividualFlags(
            @RequestParam String email,
            @RequestBody ChatFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateIndividualFlags(email, dto));
    }

    // ── ADMIN: GET /api/chat-feature-flags/admin/user/{email} ────────────────
    // organizationId comes ONLY from the caller's own JWT — never from
    // path/body/params — this keeps an admin scoped to their own org's users.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/user/{email}")
    public ResponseEntity<ChatFeatureFlagsDTO> getAdminUserFlags(
            @PathVariable String email,
            HttpServletRequest request) {
        String organizationId = currentOrgId(request);
        return ResponseEntity.ok(featureFlagsService.getAdminUserFlags(organizationId, email));
    }

    // ── ADMIN: PUT /api/chat-feature-flags/admin/user/{email} ────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/user/{email}")
    public ResponseEntity<ChatFeatureFlagsDTO> updateAdminUserFlags(
            @PathVariable String email,
            @RequestBody ChatFeatureFlagsDTO dto,
            HttpServletRequest request) {
        String organizationId = currentOrgId(request);
        return ResponseEntity.ok(featureFlagsService.updateAdminUserFlags(organizationId, email, dto));
    }

    private String currentOrgId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7);
        return jwtUtil.extractOrganizationId(token);
    }
}