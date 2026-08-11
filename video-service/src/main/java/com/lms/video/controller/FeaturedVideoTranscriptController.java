package com.lms.video.controller;

import com.lms.video.dto.TranscriptResponse;
import com.lms.video.model.FeaturedTranscriptSegment;
import com.lms.video.model.FeaturedVideoTranscript;
import com.lms.video.model.TranscriptStatus;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import org.springframework.web.bind.annotation.*;
import com.lms.video.model.TranscriptSourceType;
import java.util.List;
import java.util.Optional;

// Separate from FeaturedSessionVideoController on purpose — this way the
// existing controller and its endpoints stay byte-for-byte unchanged.
// Shares the same base path; Spring is fine with two controllers doing that
// as long as the concrete mappings (here, "/{sessionId}/transcript") don't collide.
@RestController
@RequestMapping("/api/video/v1/featured/session")
public class FeaturedVideoTranscriptController {

    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;

    public FeaturedVideoTranscriptController(FeaturedVideoTranscriptRepository transcriptRepo,
                                              FeaturedTranscriptSegmentRepository segmentRepo) {
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
    }

    // No @PreAuthorize — matches the access level of the existing
    // /stream/{fileName} endpoint (also unrestricted beyond normal auth).
    @GetMapping("/{sessionId}/transcript")
    public TranscriptResponse getTranscript(@PathVariable Long sessionId) {
//        Optional<FeaturedVideoTranscript> transcriptOpt = transcriptRepo.findBySessionId(sessionId);
    	Optional<FeaturedVideoTranscript> transcriptOpt =
    	        transcriptRepo.findBySessionIdAndSourceType(sessionId, TranscriptSourceType.FEATURED);
        if (transcriptOpt.isEmpty()) {
            return new TranscriptResponse("NONE", null, null, List.of());
        }

        FeaturedVideoTranscript transcript = transcriptOpt.get();

        if (transcript.getStatus() == TranscriptStatus.READY) {
            List<FeaturedTranscriptSegment> segments =
                    segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(transcript.getId());
            List<TranscriptResponse.SegmentDto> segmentDtos = segments.stream()
                    .map(s -> new TranscriptResponse.SegmentDto(s.getStartSeconds(), s.getEndSeconds(), s.getText()))
                    .toList();
            return new TranscriptResponse("READY", transcript.getLanguage(), null, segmentDtos);
        }

        if (transcript.getStatus() == TranscriptStatus.FAILED) {
            return new TranscriptResponse("FAILED", transcript.getLanguage(), transcript.getErrorMessage(), List.of());
        }

        // PROCESSING (or a NONE row, which shouldn't normally exist once upload has run)
        return new TranscriptResponse(transcript.getStatus().name(), transcript.getLanguage(), null, List.of());
    }
}