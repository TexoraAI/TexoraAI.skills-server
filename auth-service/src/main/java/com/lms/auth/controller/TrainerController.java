package com.lms.auth.controller;

import com.lms.auth.dto.TrainerApplyRequest;
import com.lms.auth.dto.TrainerResponse;
import com.lms.auth.service.TrainerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trainer")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    // ✅ Trainer Apply
    @PostMapping("/apply")
    public ResponseEntity<Map<String, String>> apply(@RequestBody TrainerApplyRequest request) {
        trainerService.apply(request);
        return ResponseEntity.ok(Map.of("message", "Trainer application submitted. Waiting for approval."));
    }

    // ✅ Admin: Pending Trainers
    @GetMapping("/pending")
    public ResponseEntity<List<TrainerResponse>> pending() {
        return ResponseEntity.ok(trainerService.getPendingTrainers());
    }

    // ✅ Admin: Approve Trainer
    @PutMapping("/approve/{id}")
    public ResponseEntity<Map<String, String>> approve(@PathVariable Long id) {
        trainerService.approveTrainer(id);
        return ResponseEntity.ok(Map.of("message", "Trainer approved successfully"));
    }

    // ✅ Admin: Reject Trainer (optional)
    @PutMapping("/reject/{id}")
    public ResponseEntity<Map<String, String>> reject(@PathVariable Long id) {
        trainerService.rejectTrainer(id);
        return ResponseEntity.ok(Map.of("message", "Trainer rejected successfully"));
    }
}
