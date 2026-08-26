package com.lms.live_session.controller;

import com.lms.live_session.dto.ScheduleRequestDTO;
import com.lms.live_session.dto.ScheduleResponseDTO;
import com.lms.live_session.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

//    @PostMapping
//    public ResponseEntity<?> createSchedule(@RequestBody ScheduleRequestDTO dto, Authentication auth) {
//        try {
//            return ResponseEntity.ok(scheduleService.createSchedule(dto, auth.getName()));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        }
//    }
    @PostMapping
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleRequestDTO dto, Authentication auth) {
        try {
            String creatorRole = extractRole(auth);
            return ResponseEntity.ok(scheduleService.createSchedule(dto, auth.getName(), creatorRole));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    private String extractRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .findFirst()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .orElse("USER");
    }

    @GetMapping
    public ResponseEntity<List<ScheduleResponseDTO>> getMySchedules(
            @RequestParam(required = false) String tab, Authentication auth) {
        try {
            if (tab != null) {
                return ResponseEntity.ok(scheduleService.getSchedulesByTab(auth.getName(), tab));
            }
            return ResponseEntity.ok(scheduleService.getMySchedules(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getScheduleById(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(scheduleService.getScheduleById(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody ScheduleRequestDTO dto, Authentication auth) {
        try {
            return ResponseEntity.ok(scheduleService.updateSchedule(id, dto, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id, Authentication auth) {
        try {
            scheduleService.deleteSchedule(id, auth.getName());
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> getSchedulesCalendar(@RequestParam(required = false) String month, Authentication auth) {
        try {
            return ResponseEntity.ok(scheduleService.getSchedulesGroupedByDate(auth.getName(), month));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) { this.error = error; }
    }
}