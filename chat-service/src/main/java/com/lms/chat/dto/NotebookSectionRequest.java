// NotebookSectionRequest.java
package com.lms.chat.dto;

public class NotebookSectionRequest {
    private String title;
    private String color;
    private int position;
    private Long notebookId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public Long getNotebookId() { return notebookId; }
    public void setNotebookId(Long notebookId) { this.notebookId = notebookId; }
}