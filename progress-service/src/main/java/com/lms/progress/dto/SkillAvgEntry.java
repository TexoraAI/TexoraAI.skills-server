package com.lms.progress.dto;

public class SkillAvgEntry {

    private String skillName;
    private double avgScore;
    private String level;   // "Beginner" / "Intermediate" / "Advanced"

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
}