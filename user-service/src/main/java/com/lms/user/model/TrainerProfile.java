//package com.lms.user.model;
//
//import jakarta.persistence.*;
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "trainer_profiles")
//public class TrainerProfile {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne
//    @JoinColumn(name = "user_id", nullable = false, unique = true)
//    private User user;
//
//    private String linkedinUrl;
//    private String country;
//    private String audienceSize;
//    private String fullTimeRole;
//    private String courseTopic;
//
//    @ElementCollection
//    @CollectionTable(name = "trainer_platforms",
//        joinColumns = @JoinColumn(name = "trainer_profile_id"))
//    @Column(name = "platform")
//    private List<String> platforms = new ArrayList<>();
//
//    // getters & setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//    public User getUser() { return user; }
//    public void setUser(User user) { this.user = user; }
//    public String getLinkedinUrl() { return linkedinUrl; }
//    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
//    public String getCountry() { return country; }
//    public void setCountry(String country) { this.country = country; }
//    public String getAudienceSize() { return audienceSize; }
//    public void setAudienceSize(String audienceSize) { this.audienceSize = audienceSize; }
//    public String getFullTimeRole() { return fullTimeRole; }
//    public void setFullTimeRole(String fullTimeRole) { this.fullTimeRole = fullTimeRole; }
//    public String getCourseTopic() { return courseTopic; }
//    public void setCourseTopic(String courseTopic) { this.courseTopic = courseTopic; }
//    public List<String> getPlatforms() { return platforms; }
//    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }
//}

package com.lms.user.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

// WHY: Trainer professional context used to match trainers to batches and display credentials
@Entity
@Table(name = "trainer_profiles",
    indexes = {
        // WHY: Trainer profile is always fetched by user_id — same as student profile pattern
        @Index(name = "idx_trainer_profiles_user_id", columnList = "user_id", unique = true)
    })
public class TrainerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // OPTIMIZATION: Added FetchType.LAZY — was defaulting to EAGER, causing user JOIN on every load
    // WHY: Trainer profile is only loaded on trainer's own profile page, not on every user query
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // WHY: Shown on trainer bio page visible to enrolled students
    private String linkedinUrl;
    private String country;
    // WHY: Used by admin to assign appropriate batch sizes based on trainer experience
    private String audienceSize;
    private String fullTimeRole;
    private String courseTopic;

    // WHY: Platforms list shows where trainer has previously taught (Udemy, YouTube, etc.)
    @ElementCollection
    @CollectionTable(name = "trainer_platforms",
        joinColumns = @JoinColumn(name = "trainer_profile_id"))
    @Column(name = "platform")
    private List<String> platforms = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getLinkedinUrl() { return linkedinUrl; }
    public void setLinkedinUrl(String linkedinUrl) { this.linkedinUrl = linkedinUrl; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getAudienceSize() { return audienceSize; }
    public void setAudienceSize(String audienceSize) { this.audienceSize = audienceSize; }
    public String getFullTimeRole() { return fullTimeRole; }
    public void setFullTimeRole(String fullTimeRole) { this.fullTimeRole = fullTimeRole; }
    public String getCourseTopic() { return courseTopic; }
    public void setCourseTopic(String courseTopic) { this.courseTopic = courseTopic; }
    public List<String> getPlatforms() { return platforms; }
    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }
}