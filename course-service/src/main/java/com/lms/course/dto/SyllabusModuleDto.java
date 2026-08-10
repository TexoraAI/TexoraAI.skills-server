package com.lms.course.dto;

import java.util.List;

public class SyllabusModuleDto {

    private Long id;
    private String title;
    private Integer orderIndex;
    private List<SyllabusSessionDto> sessions;

    public SyllabusModuleDto() {
    }

    public SyllabusModuleDto(Long id, String title, Integer orderIndex, List<SyllabusSessionDto> sessions) {
        this.id = id;
        this.title = title;
        this.orderIndex = orderIndex;
        this.sessions = sessions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<SyllabusSessionDto> getSessions() {
        return sessions;
    }

    public void setSessions(List<SyllabusSessionDto> sessions) {
        this.sessions = sessions;
    }
}