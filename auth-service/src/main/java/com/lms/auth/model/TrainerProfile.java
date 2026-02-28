package com.lms.auth.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainer_profiles")
public class TrainerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to users table
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String linkedinUrl;

    @Column(nullable = false)
    private String country;

    // checkbox list: Blog, YouTube, Podcast, etc.
    @ElementCollection
    @CollectionTable(name = "trainer_platforms", joinColumns = @JoinColumn(name = "trainer_profile_id"))
    @Column(name = "platform")
    private List<String> platforms = new ArrayList<>();

    @Column(nullable = false)
    private String audienceSize;

    @Column(nullable = false)
    private String fullTimeRole; // "Yes" or "No"

    @Column(nullable = false)
    private String courseTopic;

    public TrainerProfile() {
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public void setLinkedinUrl(String linkedinUrl) {
        this.linkedinUrl = linkedinUrl;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public String getAudienceSize() {
        return audienceSize;
    }

    public void setAudienceSize(String audienceSize) {
        this.audienceSize = audienceSize;
    }

    public String getFullTimeRole() {
        return fullTimeRole;
    }

    public void setFullTimeRole(String fullTimeRole) {
        this.fullTimeRole = fullTimeRole;
    }

    public String getCourseTopic() {
        return courseTopic;
    }

    public void setCourseTopic(String courseTopic) {
        this.courseTopic = courseTopic;
    }
}
