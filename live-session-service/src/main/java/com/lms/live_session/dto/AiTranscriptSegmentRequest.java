package com.lms.live_session.dto;
public class AiTranscriptSegmentRequest {
    private String text;
    private String speakerName;
    private Integer startedAtSecond;
 
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
 
    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }
 
    public Integer getStartedAtSecond() { return startedAtSecond; }
    public void setStartedAtSecond(Integer startedAtSecond) { this.startedAtSecond = startedAtSecond; }
}