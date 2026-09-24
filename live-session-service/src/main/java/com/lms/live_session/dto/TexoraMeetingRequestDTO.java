package com.lms.live_session.dto;

public class TexoraMeetingRequestDTO {
    private String topic;
    private String startTime;
    private Integer durationMinutes;
    private String externalRef;
    private TexoraContextDTO context;

    public TexoraMeetingRequestDTO() {}

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getExternalRef() { return externalRef; }
    public void setExternalRef(String externalRef) { this.externalRef = externalRef; }

    public TexoraContextDTO getContext() { return context; }
    public void setContext(TexoraContextDTO context) { this.context = context; }
}