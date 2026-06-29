package com.lms.course.dto;

public class AdminStatsDTO {

    private long totalPrograms;
    private long activePrograms;
    private long inactivePrograms;
    private long totalCategories;

    public AdminStatsDTO() {
    }

    public AdminStatsDTO(long totalPrograms, long activePrograms, long inactivePrograms, long totalCategories) {
        this.totalPrograms = totalPrograms;
        this.activePrograms = activePrograms;
        this.inactivePrograms = inactivePrograms;
        this.totalCategories = totalCategories;
    }

    public long getTotalPrograms() {
        return totalPrograms;
    }

    public void setTotalPrograms(long totalPrograms) {
        this.totalPrograms = totalPrograms;
    }

    public long getActivePrograms() {
        return activePrograms;
    }

    public void setActivePrograms(long activePrograms) {
        this.activePrograms = activePrograms;
    }

    public long getInactivePrograms() {
        return inactivePrograms;
    }

    public void setInactivePrograms(long inactivePrograms) {
        this.inactivePrograms = inactivePrograms;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }
}