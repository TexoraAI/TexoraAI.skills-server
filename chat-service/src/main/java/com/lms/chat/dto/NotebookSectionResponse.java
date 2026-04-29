// NotebookSectionResponse.java
package com.lms.chat.dto;

import com.lms.chat.entity.NotebookSection;
import java.util.List;
import java.util.stream.Collectors;

public class NotebookSectionResponse {
    private Long id;
    private String title;
    private String color;
    private int position;
    private List<NotebookPageResponse> pages;

    public static NotebookSectionResponse from(NotebookSection s) {
        NotebookSectionResponse r = new NotebookSectionResponse();
        r.id       = s.getId();
        r.title    = s.getTitle();
        r.color    = s.getColor();
        r.position = s.getPosition();
        r.pages    = s.getPages().stream()
                      .map(NotebookPageResponse::from)
                      .collect(Collectors.toList());
        return r;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public List<NotebookPageResponse> getPages() { return pages; }
    public void setPages(List<NotebookPageResponse> pages) { this.pages = pages; }
}