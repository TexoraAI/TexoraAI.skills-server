package com.lms.live_session.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CreateSessionRequest {

    private String title;
    private String description;
    private Long trainerId;
    private Long batchId;
    private LocalDate date;
    private LocalTime time;
    private Integer duration;

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public Long getTrainerId() { return trainerId; }

    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public Long getBatchId() { return batchId; }

    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }

    public void setTime(LocalTime time) { this.time = time; }

    public Integer getDuration() { return duration; }

    public void setDuration(Integer duration) { this.duration = duration; }
}