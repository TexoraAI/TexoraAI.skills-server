package com.lms.course.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "syllabus_modules")
public class SyllabusModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private SyllabusWeek week;

    @Column(nullable = false)
    private String title;

    private Integer orderIndex;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderIndex ASC")
    private List<SyllabusSession> sessions = new ArrayList<>();

    public SyllabusModule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SyllabusWeek getWeek() {
        return week;
    }

    public void setWeek(SyllabusWeek week) {
        this.week = week;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public List<SyllabusSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<SyllabusSession> sessions) {
        this.sessions = sessions;
    }
}