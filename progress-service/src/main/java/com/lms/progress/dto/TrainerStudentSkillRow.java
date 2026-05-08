package com.lms.progress.dto;

import java.util.List;

public class TrainerStudentSkillRow {

    private String studentEmail;
    private double avgScore;
    private boolean needsHelp;          // true if any skill < 50
    private List<SkillEntryDTO> skills;

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }

    public boolean isNeedsHelp() { return needsHelp; }
    public void setNeedsHelp(boolean needsHelp) { this.needsHelp = needsHelp; }

    public List<SkillEntryDTO> getSkills() { return skills; }
    public void setSkills(List<SkillEntryDTO> skills) { this.skills = skills; }
}