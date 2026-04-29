// NotebookPageRequest.java
package com.lms.chat.dto;

public class NotebookPageRequest {
    private String title;
    private String content;
    private int position;
    private Long sectionId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public Long getSectionId() { return sectionId; }
    public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
}