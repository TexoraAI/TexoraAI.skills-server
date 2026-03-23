package com.lms.live_session.service;

import com.lms.live_session.entity.Recording;
import com.lms.live_session.repository.RecordingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecordingService {

    private final RecordingRepository recordingRepository;

    public RecordingService(RecordingRepository recordingRepository) {
        this.recordingRepository = recordingRepository;
    }

    public Recording saveRecording(Recording recording) {

        recording.setUploadDate(LocalDateTime.now());

        return recordingRepository.save(recording);
    }

    public List<Recording> getBatchRecordings(Long batchId) {

        return recordingRepository.findByBatchId(batchId);
    }

}