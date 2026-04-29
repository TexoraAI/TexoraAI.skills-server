//// NotebookResponse.java
//package com.lms.chat.dto;
//
//import com.lms.chat.entity.Notebook;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class NotebookResponse {
//    private Long id;
//    private String studentEmail;
//    private String title;
//    private String color;
//    private String icon;
//    private int sectionCount;
//    private int pageCount;
//    private List<NotebookSectionResponse> sections;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
// // In NotebookResponse.java, add:
//    private List<NotebookSourceResponse> sources;
//
//    // In the from() method, add:
//    
//    public static NotebookResponse from(Notebook nb) {
//        NotebookResponse r = new NotebookResponse();
//        r.id           = nb.getId();
//        r.studentEmail = nb.getStudentEmail();
//        r.title        = nb.getTitle();
//        r.color        = nb.getColor();
//        r.icon         = nb.getIcon();
//        r.createdAt    = nb.getCreatedAt();
//        r.updatedAt    = nb.getUpdatedAt();
//        r.sections     = nb.getSections().stream()
//                           .map(NotebookSectionResponse::from)
//                           .collect(Collectors.toList());
//        r.sectionCount = r.sections.size();
//        r.pageCount    = r.sections.stream()
//                           .mapToInt(s -> s.getPages().size())
//                           .sum();
//        // ✅ ADD THIS (YOU MISSED THIS PART)
//        if (nb.getSources() != null) {
//            r.sources = nb.getSources().stream()
//                    .map(NotebookSourceResponse::from)
//                    .collect(Collectors.toList());
//        }
//
//        return r;
//    }
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public String getStudentEmail() { return studentEmail; }
//    public void setStudentEmail(String e) { this.studentEmail = e; }
//    public String getTitle() { return title; }
//    public void setTitle(String title) { this.title = title; }
//    public String getColor() { return color; }
//    public void setColor(String color) { this.color = color; }
//    public String getIcon() { return icon; }
//    public void setIcon(String icon) { this.icon = icon; }
//    public int getSectionCount() { return sectionCount; }
//    public void setSectionCount(int sectionCount) { this.sectionCount = sectionCount; }
//    public int getPageCount() { return pageCount; }
//    public void setPageCount(int pageCount) { this.pageCount = pageCount; }
//    public List<NotebookSectionResponse> getSections() { return sections; }
//    public void setSections(List<NotebookSectionResponse> sections) { this.sections = sections; }
//    public LocalDateTime getCreatedAt() { return createdAt; }
//    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
//    public LocalDateTime getUpdatedAt() { return updatedAt; }
//    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
//}

package com.lms.chat.dto;

import com.lms.chat.entity.Notebook;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NotebookResponse {

    private Long id;
    private String studentEmail;
    private String title;
    private String color;
    private String icon;
    private int sectionCount;
    private int pageCount;
    private int sourceCount;
    private List<NotebookSectionResponse> sections;
    private List<NotebookSourceResponse> sources;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NotebookResponse from(Notebook nb) {
        NotebookResponse r = new NotebookResponse();
        r.id           = nb.getId();
        r.studentEmail = nb.getStudentEmail();
        r.title        = nb.getTitle();
        r.color        = nb.getColor();
        r.icon         = nb.getIcon();
        r.createdAt    = nb.getCreatedAt();
        r.updatedAt    = nb.getUpdatedAt();

        r.sections = nb.getSections().stream()
                       .map(NotebookSectionResponse::from)
                       .collect(Collectors.toList());

        r.sectionCount = r.sections.size();

        r.pageCount = r.sections.stream()
                        .mapToInt(s -> s.getPages().size())
                        .sum();

        r.sources = nb.getSources() != null
                      ? nb.getSources().stream()
                          .map(NotebookSourceResponse::from)
                          .collect(Collectors.toList())
                      : List.of();

        r.sourceCount = r.sources.size();

        return r;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public int getSectionCount() { return sectionCount; }
    public void setSectionCount(int sectionCount) { this.sectionCount = sectionCount; }

    public int getPageCount() { return pageCount; }
    public void setPageCount(int pageCount) { this.pageCount = pageCount; }

    public int getSourceCount() { return sourceCount; }
    public void setSourceCount(int sourceCount) { this.sourceCount = sourceCount; }

    public List<NotebookSectionResponse> getSections() { return sections; }
    public void setSections(List<NotebookSectionResponse> sections) { this.sections = sections; }

    public List<NotebookSourceResponse> getSources() { return sources; }
    public void setSources(List<NotebookSourceResponse> sources) { this.sources = sources; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}