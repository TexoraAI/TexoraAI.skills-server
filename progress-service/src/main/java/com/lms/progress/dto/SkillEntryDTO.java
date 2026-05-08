package com.lms.progress.dto;

import java.time.Instant;

public class SkillEntryDTO {

    private String skillName;
    private double quizScore;
    private double assignmentScore;
    private double videoScore;
    private double overallScore;
    private boolean isWeak;
    private boolean isStrong;
    private String level;        // "Beginner" / "Intermediate" / "Advanced"
    private Instant updatedAt;

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public double getQuizScore() { return quizScore; }
    public void setQuizScore(double quizScore) { this.quizScore = quizScore; }

    public double getAssignmentScore() { return assignmentScore; }
    public void setAssignmentScore(double assignmentScore) { this.assignmentScore = assignmentScore; }

    public double getVideoScore() { return videoScore; }
    public void setVideoScore(double videoScore) { this.videoScore = videoScore; }

    public double getOverallScore() { return overallScore; }
    public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

    public boolean isWeak() { return isWeak; }
    public void setWeak(boolean weak) { isWeak = weak; }

    public boolean isStrong() { return isStrong; }
    public void setStrong(boolean strong) { isStrong = strong; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}