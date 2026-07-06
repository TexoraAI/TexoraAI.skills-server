package com.lms.course.dto;

public class ExtractedSessionDto {
    private String title;
    private String type;     // Video, Live, Assignment, Quiz, Reading
    private String duration;

    public ExtractedSessionDto() {}
    public ExtractedSessionDto(String title, String type, String duration) {
        this.title = title;
        this.type = type;
        this.duration = duration;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
}