package com.lms.progress.dto;

import java.util.List;

public class StudentSkillMapResponse {

    private String studentEmail;
    private Long   batchId;
    private int    totalSkills;
    private int    strongCount;
    private int    weakCount;
    private double avgScore;
    private List<SkillEntryDTO> skills;

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public int getTotalSkills() { return totalSkills; }
    public void setTotalSkills(int totalSkills) { this.totalSkills = totalSkills; }

    public int getStrongCount() { return strongCount; }
    public void setStrongCount(int strongCount) { this.strongCount = strongCount; }

    public int getWeakCount() { return weakCount; }
    public void setWeakCount(int weakCount) { this.weakCount = weakCount; }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }

    public List<SkillEntryDTO> getSkills() { return skills; }
    public void setSkills(List<SkillEntryDTO> skills) { this.skills = skills; }
}