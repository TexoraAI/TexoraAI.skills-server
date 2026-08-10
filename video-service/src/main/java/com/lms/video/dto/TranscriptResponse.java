package com.lms.video.dto;

import java.util.List;

public record TranscriptResponse(
        String status,
        String language,
        String errorMessage,
        List<SegmentDto> segments
) {
    public record SegmentDto(Double startSeconds, Double endSeconds, String text) {}
}