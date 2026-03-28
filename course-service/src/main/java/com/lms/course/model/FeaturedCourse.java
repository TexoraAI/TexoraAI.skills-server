//package com.lms.course.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "featured_courses")
//public class FeaturedCourse {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String title;
//    private boolean onDemand;
//    private boolean featured;
//
//    private String thumbnail;
//
//    private String level;
//    private String duration;
//    private Double rating;
//    private String students;
//
//    @Column(columnDefinition = "TEXT")
//    private String tags;
//
//    @Column(columnDefinition = "TEXT")
//    private String instructors;
//
//    // ===== GETTERS & SETTERS =====
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public boolean isOnDemand() {
//        return onDemand;
//    }
//
//    public void setOnDemand(boolean onDemand) {
//        this.onDemand = onDemand;
//    }
//
//    public boolean isFeatured() {
//        return featured;
//    }
//
//    public void setFeatured(boolean featured) {
//        this.featured = featured;
//    }
//
//    public String getThumbnail() {
//        return thumbnail;
//    }
//
//    public void setThumbnail(String thumbnail) {
//        this.thumbnail = thumbnail;
//    }
//
//    public String getLevel() {
//        return level;
//    }
//
//    public void setLevel(String level) {
//        this.level = level;
//    }
//
//    public String getDuration() {
//        return duration;
//    }
//
//    public void setDuration(String duration) {
//        this.duration = duration;
//    }
//
//    public Double getRating() {
//        return rating;
//    }
//
//    public void setRating(Double rating) {
//        this.rating = rating;
//    }
//
//    public String getStudents() {
//        return students;
//    }
//
//    public void setStudents(String students) {
//        this.students = students;
//    }
//
//    public String getTags() {
//        return tags;
//    }
//
//    public void setTags(String tags) {
//        this.tags = tags;
//    }
//
//    public String getInstructors() {
//        return instructors;
//    }
//
//    public void setInstructors(String instructors) {
//        this.instructors = instructors;
//    }
//}
package com.lms.course.model;

import jakarta.persistence.*;

@Entity
@Table(name = "featured_courses")
public class FeaturedCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private boolean onDemand;
    private boolean featured;

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    private String level;
    private String duration;
    private Double rating;
    private String students;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String instructors;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isOnDemand() {
        return onDemand;
    }

    public void setOnDemand(boolean onDemand) {
        this.onDemand = onDemand;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getStudents() {
        return students;
    }

    public void setStudents(String students) {
        this.students = students;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getInstructors() {
        return instructors;
    }

    public void setInstructors(String instructors) {
        this.instructors = instructors;
    }
}