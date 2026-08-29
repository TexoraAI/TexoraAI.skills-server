package com.lms.progress.dto;

public class RoadmapUpgradedMentorRequestDto {

    private Long syllabusId;
    private String message;

    public RoadmapUpgradedMentorRequestDto() {
    }

    public RoadmapUpgradedMentorRequestDto(Long syllabusId, String message) {
        this.syllabusId = syllabusId;
        this.message = message;
    }

    public Long getSyllabusId() {
        return syllabusId;
    }

    public void setSyllabusId(Long syllabusId) {
        this.syllabusId = syllabusId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
