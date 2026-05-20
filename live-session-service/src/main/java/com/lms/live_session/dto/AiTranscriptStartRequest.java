package com.lms.live_session.dto;
 
// ── Start Request ──────────────────────────────────────────────────────────────
// POST /api/v1/ai-companion/transcripts/start
public class AiTranscriptStartRequest {
    private Long liveSessionId; // optional
    private String title;       // optional
 
    public Long getLiveSessionId() { return liveSessionId; }
    public void setLiveSessionId(Long liveSessionId) { this.liveSessionId = liveSessionId; }
 
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}