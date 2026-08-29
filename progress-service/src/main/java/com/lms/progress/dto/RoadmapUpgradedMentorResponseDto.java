package com.lms.progress.dto;

import java.util.ArrayList;
import java.util.List;

public class RoadmapUpgradedMentorResponseDto {

    private String reply;
    private List<String> suggestedFollowUps = new ArrayList<>();

    public RoadmapUpgradedMentorResponseDto() {
    }

    public RoadmapUpgradedMentorResponseDto(String reply, List<String> suggestedFollowUps) {
        this.reply = reply;
        this.suggestedFollowUps = suggestedFollowUps != null ? suggestedFollowUps : new ArrayList<>();
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getSuggestedFollowUps() {
        return suggestedFollowUps;
    }

    public void setSuggestedFollowUps(List<String> suggestedFollowUps) {
        this.suggestedFollowUps = suggestedFollowUps;
    }
}
