//// Path: src/main/java/com/lms/user/model/StudentProfile.java
//package com.lms.user.model;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "student_profiles")
//public class StudentProfile {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false, unique = true)
//    private User user;
//
//    @Column(name = "mobile_number")
//    private String mobileNumber;
//
//    @Column(name = "date_of_birth")
//    private String dateOfBirth;
//
//    @Column(name = "gender")
//    private String gender;
//
//    @Column(name = "city")
//    private String city;
//
//    @Column(name = "state")
//    private String state;
//
//    @Column(name = "country")
//    private String country;
//
//    @Column(name = "qualification")
//    private String qualification;
//
//    @Column(name = "college_name")
//    private String collegeName;
//
//    @Column(name = "year_of_passing")
//    private String yearOfPassing;
//
//    @Column(name = "domain")
//    private String domain;
//
//    @Column(name = "experience")
//    private String experience;
//
//    public StudentProfile() {}
//
//    // Getters & Setters
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public User getUser() { return user; }
//    public void setUser(User user) { this.user = user; }
//
//    public String getMobileNumber() { return mobileNumber; }
//    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
//
//    public String getDateOfBirth() { return dateOfBirth; }
//    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
//
//    public String getGender() { return gender; }
//    public void setGender(String gender) { this.gender = gender; }
//
//    public String getCity() { return city; }
//    public void setCity(String city) { this.city = city; }
//
//    public String getState() { return state; }
//    public void setState(String state) { this.state = state; }
//
//    public String getCountry() { return country; }
//    public void setCountry(String country) { this.country = country; }
//
//    public String getQualification() { return qualification; }
//    public void setQualification(String qualification) { this.qualification = qualification; }
//
//    public String getCollegeName() { return collegeName; }
//    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
//
//    public String getYearOfPassing() { return yearOfPassing; }
//    public void setYearOfPassing(String yearOfPassing) { this.yearOfPassing = yearOfPassing; }
//
//    public String getDomain() { return domain; }
//    public void setDomain(String domain) { this.domain = domain; }
//
//    public String getExperience() { return experience; }
//    public void setExperience(String experience) { this.experience = experience; }
//}






package com.lms.user.model;

import jakarta.persistence.*;

// WHY: Extended student demographic data collected during LMS onboarding for admin analytics
@Entity
@Table(name = "student_profiles",
    indexes = {
        // WHY: Every student profile lookup is by user_id or user.email (JOIN through user_id)
        @Index(name = "idx_student_profiles_user_id", columnList = "user_id", unique = true)
    })
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHY: LAZY — profile is not always needed; loaded only on explicit profile page requests
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "mobile_number") private String mobileNumber;
    @Column(name = "date_of_birth") private String dateOfBirth;
    @Column(name = "gender") private String gender;
    @Column(name = "city") private String city;
    @Column(name = "state") private String state;
    @Column(name = "country") private String country;
    @Column(name = "qualification") private String qualification;
    @Column(name = "college_name") private String collegeName;
    @Column(name = "year_of_passing") private String yearOfPassing;
    // WHY: Domain preference used by admin to assign students to relevant batches
    @Column(name = "domain") private String domain;
    // WHY: Prior experience determines batch/course track assignment by admin
    @Column(name = "experience") private String experience;

    public StudentProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getQualification() { return qualification; }
    public void setQualification(String qualification) { this.qualification = qualification; }
    public String getCollegeName() { return collegeName; }
    public void setCollegeName(String collegeName) { this.collegeName = collegeName; }
    public String getYearOfPassing() { return yearOfPassing; }
    public void setYearOfPassing(String yearOfPassing) { this.yearOfPassing = yearOfPassing; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
}