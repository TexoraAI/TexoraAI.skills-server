package com.lms.live_session.controller;

import com.lms.live_session.entity.Recording;
import com.lms.live_session.service.RecordingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recordings")
public class RecordingController {

    private final RecordingService recordingService;

    public RecordingController(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    @PostMapping
    public Recording uploadRecording(@RequestBody Recording recording) {

        return recordingService.saveRecording(recording);
    }

    @GetMapping("/batch/{batchId}")
    public List<Recording> getBatchRecordings(
            @PathVariable Long batchId) {

        return recordingService.getBatchRecordings(batchId);
    }
}