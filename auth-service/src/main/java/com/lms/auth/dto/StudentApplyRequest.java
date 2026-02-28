package com.lms.auth.dto;

public class StudentApplyRequest {

    private String fullName;
    private String email;
    private String password;

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

    public StudentApplyRequest() {}

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
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

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
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
