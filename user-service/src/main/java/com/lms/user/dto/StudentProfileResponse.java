//// Path: src/main/java/com/lms/user/dto/StudentProfileResponse.java
//package com.lms.user.dto;
//
//public class StudentProfileResponse {
//
//    private String mobileNumber;
//    private String dateOfBirth;
//    private String gender;
//    private String city;
//    private String state;
//    private String country;
//    private String qualification;
//    private String collegeName;
//    private String yearOfPassing;
//    private String domain;
//    private String experience;
//
//    public StudentProfileResponse() {}
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


package com.lms.user.dto;

import java.io.Serializable;

public class StudentProfileResponse implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public StudentProfileResponse() {}

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