package com.lms.auth.model;

import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Student Form Fields
    @Column(nullable = false)
    private String fullName;

    private String mobileNumber;
    private String dateOfBirth;
    private String gender;

    private String city;
    private String state;
    private String country;

    private String qualification;
    private String collegeName;
    private String yearOfPassing;

    private String domain;
    private String experience;

    public StudentProfile() {}

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getFullName() {
        return fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public String getQualification() {
        return qualification;
    }

    public String getCollegeName() {
        return collegeName;
    }

    public String getYearOfPassing() {
        return yearOfPassing;
    }

    public String getDomain() {
        return domain;
    }

    public String getExperience() {
        return experience;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public void setCollegeName(String collegeName) {
        this.collegeName = collegeName;
    }

    public void setYearOfPassing(String yearOfPassing) {
        this.yearOfPassing = yearOfPassing;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }
}
