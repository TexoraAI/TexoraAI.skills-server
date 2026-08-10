package com.lms.course.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "syllabus_weeks")
public class SyllabusWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id")
    private FeaturedProgram program;

    @Column(nullable = false)
    private Integer weekNumber;

    @Column(nullable = false)
    private String title;

    private String dateRange;

//    @ElementCollection
 

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "syllabus_week_items", joinColumns = @JoinColumn(name = "week_id"))
    @Column(name = "item", columnDefinition = "TEXT")
    private List<String> items = new ArrayList<>();

    // NEW: Week -> Module -> Session hierarchy, additive alongside the flat `items` list above.
    // `items` is NOT removed and continues to work exactly as before.
    @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("orderIndex ASC")
    private List<SyllabusModule> modules = new ArrayList<>();

    public SyllabusWeek() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FeaturedProgram getProgram() {
        return program;
    }

    public void setProgram(FeaturedProgram program) {
        this.program = program;
    }

    public Integer getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<SyllabusModule> getModules() {
        return modules;
    }

    public void setModules(List<SyllabusModule> modules) {
        this.modules = modules;
    }
}