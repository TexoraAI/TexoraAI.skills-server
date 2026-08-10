package com.lms.course.dto;

import java.util.List;

public class SyllabusWeekDto {

    private Long id;
    private Integer weekNumber;
    private String title;
    private String dateRange;
    private List<String> items;

    // NEW: additive alongside `items`. Populated only by the new overload below;
    // existing callers using the 5-arg constructor get modules == null, unchanged behavior.
    private List<SyllabusModuleDto> modules;

    public SyllabusWeekDto() {
    }

    public SyllabusWeekDto(Long id, Integer weekNumber, String title, String dateRange, List<String> items) {
        this.id = id;
        this.weekNumber = weekNumber;
        this.title = title;
        this.dateRange = dateRange;
        this.items = items;
    }

    // NEW overload: same as above, plus modules
    public SyllabusWeekDto(Long id, Integer weekNumber, String title, String dateRange, List<String> items,
                            List<SyllabusModuleDto> modules) {
        this.id = id;
        this.weekNumber = weekNumber;
        this.title = title;
        this.dateRange = dateRange;
        this.items = items;
        this.modules = modules;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<SyllabusModuleDto> getModules() {
        return modules;
    }

    public void setModules(List<SyllabusModuleDto> modules) {
        this.modules = modules;
    }
}