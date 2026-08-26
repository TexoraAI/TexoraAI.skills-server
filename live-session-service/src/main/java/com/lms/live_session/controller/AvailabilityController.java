package com.lms.live_session.controller;

import com.lms.live_session.dto.AvailabilitySlotRequestDTO;
import com.lms.live_session.dto.AvailabilitySlotResponseDTO;
import com.lms.live_session.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<?> createAvailability(@RequestBody AvailabilitySlotRequestDTO dto, Authentication auth) {
        try {
            return ResponseEntity.ok(availabilityService.createAvailability(dto, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, List<AvailabilitySlotResponseDTO>>> getMyAvailability(Authentication auth) {
        try {
            return ResponseEntity.ok(availabilityService.getWeeklyAvailability(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new HashMap<>());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSlotById(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(availabilityService.getSlotById(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAvailability(@PathVariable Long id,
                                                 @RequestBody AvailabilitySlotRequestDTO dto,
                                                 Authentication auth) {
        try {
            return ResponseEntity.ok(availabilityService.updateAvailability(id, dto, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAvailability(@PathVariable Long id, Authentication auth) {
        try {
            availabilityService.deleteAvailability(id, auth.getName());
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAvailability(
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime,
            Authentication auth) {
        try {
            LocalDate d = LocalDate.parse(date);
            LocalTime st = LocalTime.parse(startTime);
            LocalTime et = LocalTime.parse(endTime);
            boolean available = availabilityService.isAvailableOnDate(auth.getName(), d, st, et);
            return ResponseEntity.ok(Map.of("available", available));
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