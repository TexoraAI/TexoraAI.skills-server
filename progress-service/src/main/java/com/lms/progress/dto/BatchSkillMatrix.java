package com.lms.progress.dto;

import java.util.Map;

public class BatchSkillMatrix {

    private String skillName;
    // key = batchId, value = average score for that skill in that batch
    // e.g. { 1: 74.0, 2: 52.0, 3: 68.0 }
    private Map<Long, Double> batchScores;

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public Map<Long, Double> getBatchScores() { return batchScores; }
    public void setBatchScores(Map<Long, Double> batchScores) { this.batchScores = batchScores; }
}