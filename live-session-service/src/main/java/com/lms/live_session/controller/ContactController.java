package com.lms.live_session.controller;

import com.lms.live_session.dto.ContactRequestDTO;
import com.lms.live_session.dto.ContactResponseDTO;
import com.lms.live_session.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<?> createContact(@RequestBody ContactRequestDTO dto, Authentication auth) {
        try {
            return ResponseEntity.ok(
                    contactService.createContact(dto, auth.getName(), auth.getPrincipal().toString())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ContactResponseDTO>> getMyContacts(Authentication auth) {
        return ResponseEntity.ok(contactService.getMyContacts(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getContactById(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(contactService.getContactById(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(@PathVariable Long id, @RequestBody ContactRequestDTO dto, Authentication auth) {
        try {
            return ResponseEntity.ok(contactService.updateContact(id, dto, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id, Authentication auth) {
        try {
            contactService.deleteContact(id, auth.getName());
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContactResponseDTO>> searchContacts(@RequestParam String q, Authentication auth) {
        try {
            return ResponseEntity.ok(contactService.searchContacts(auth.getName(), q));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getEmailSuggestions(@RequestParam String q, Authentication auth) {
        try {
            return ResponseEntity.ok(contactService.getEmailSuggestions(auth.getName(), q));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}