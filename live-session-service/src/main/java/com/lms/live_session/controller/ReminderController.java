package com.lms.live_session.controller;

import com.lms.live_session.dto.ReminderRequestDTO;
import com.lms.live_session.dto.ReminderResponseDTO;
import com.lms.live_session.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    @Autowired
    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<?> createReminder(@RequestBody ReminderRequestDTO dto, Authentication auth) {
        try {
            return ResponseEntity.ok(reminderService.createReminder(dto, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ReminderResponseDTO>> getMyReminders(Authentication auth) {
        return ResponseEntity.ok(reminderService.getMyReminders(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReminderById(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(reminderService.getReminderById(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReminder(@PathVariable Long id, @RequestBody ReminderRequestDTO dto, Authentication auth) {
        try {
            return ResponseEntity.ok(reminderService.updateReminder(id, dto, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReminder(@PathVariable Long id, Authentication auth) {
        try {
            reminderService.deleteReminder(id, auth.getName());
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/dismiss")
    public ResponseEntity<?> dismissReminder(@PathVariable Long id, Authentication auth) {
        try {
            reminderService.dismissReminder(id, auth.getName());
            return ResponseEntity.ok(Map.of("dismissed", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}