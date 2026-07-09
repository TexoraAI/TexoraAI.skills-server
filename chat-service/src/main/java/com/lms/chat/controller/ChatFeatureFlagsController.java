package com.lms.chat.controller;

import com.lms.chat.dto.ChatFeatureFlagsDTO;
import com.lms.chat.service.ChatFeatureFlagsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat-feature-flags")
public class ChatFeatureFlagsController {

    private final ChatFeatureFlagsService featureFlagsService;

    public ChatFeatureFlagsController(ChatFeatureFlagsService featureFlagsService) {
        this.featureFlagsService = featureFlagsService;
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
}