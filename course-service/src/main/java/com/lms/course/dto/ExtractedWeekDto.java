package com.lms.course.dto;

import java.util.ArrayList;
import java.util.List;

public class ExtractedWeekDto {
    private String title;
    private List<ExtractedModuleDto> modules = new ArrayList<>();

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<ExtractedModuleDto> getModules() { return modules; }
    public void setModules(List<ExtractedModuleDto> modules) { this.modules = modules; }
}