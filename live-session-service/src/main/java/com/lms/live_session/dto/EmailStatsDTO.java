package com.lms.live_session.dto;

public class EmailStatsDTO {

    private Integer unread;
    private Integer sent;
    private Integer drafts;

    public EmailStatsDTO() {
    }

    public EmailStatsDTO(Integer unread, Integer sent, Integer drafts) {
        this.unread = unread;
        this.sent = sent;
        this.drafts = drafts;
    }

    public Integer getUnread() {
        return unread;
    }

    public void setUnread(Integer unread) {
        this.unread = unread;
    }

    public Integer getSent() {
        return sent;
    }

    public void setSent(Integer sent) {
        this.sent = sent;
    }

    public Integer getDrafts() {
        return drafts;
    }

    public void setDrafts(Integer drafts) {
        this.drafts = drafts;
    }
}