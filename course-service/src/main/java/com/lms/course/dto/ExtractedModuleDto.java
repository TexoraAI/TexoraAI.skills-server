package com.lms.course.dto;

import java.util.ArrayList;
import java.util.List;

public class ExtractedModuleDto {
    private String title;
    private List<ExtractedSessionDto> sessions = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<ExtractedSessionDto> getSessions() { return sessions; }
    public void setSessions(List<ExtractedSessionDto> sessions) { this.sessions = sessions; }
}