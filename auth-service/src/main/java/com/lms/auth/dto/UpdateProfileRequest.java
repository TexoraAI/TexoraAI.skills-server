package com.lms.auth.dto;

import java.util.List;
public class UpdateProfileRequest {

    /** "student" | "trainer" | "Manager" */
    private String role;

    // ── STUDENT ───────────────────────────────────────────────────
    // Matches StudentDetailsTab fields in ProfilePage.jsx

    /** Full mobile with dial code, e.g. "+919876543210" */
    private String mobileNumber;

    /** ISO date string, e.g. "1999-08-15" */
    private String dateOfBirth;

    /** "Male" | "Female" | "Other" | "Prefer not to say" */
    private String gender;

    private String city;
    private String state;
    private String country;

    /** e.g. "B.Tech", "MBA" */
    private String qualification;

    /** e.g. "IIT Delhi" — matches dashboard key `collegeName` */
    private String collegeName;

    /** 4-digit year string, e.g. "2023" */
    private String yearOfPassing;

    /** e.g. "Full Stack", "Data Science" */
    private String domain;

    /** e.g. "Fresher", "2 years" */
    private String experience;

    // ── TRAINER ───────────────────────────────────────────────────
    // Matches TrainerDetailsTab fields in ProfilePage.jsx

    private String linkedinUrl;

    /** e.g. "React, Python, AWS" — matches `courseTopic` */
    private String courseTopic;

    /** "0–1K" | "1K–10K" | "10K–100K" | "100K+" */
    private String audienceSize;

    /** "Yes" | "No" */
    private String fullTimeRole;

    /**
     * Comma-separated platform list, e.g. "YouTube,Udemy,IlmOra"
     * The dashboard stores this as List<String>; serialise on the
     * frontend with platforms.join(",") and split(",") on the backend.
     */

private List<String> platforms;


    // ── MANAGER / BUSINESS ────────────────────────────────────────
    // Matches AdminDetailsTab (business) fields in ProfilePage.jsx

    /** e.g. "Acme Corp" — replaces old `companyName` */
    private String organizationName;

    /** Website / domain, e.g. "acme.com" — replaces old `website` */
    private String websiteDomain;

    /** e.g. "admin@acme.com" */
    private String contactEmail;

    /** City + country string, e.g. "Hyderabad, India" — replaces old `city` for Manager */
    private String location;

    /**
     * Industry category from the INDUSTRIES list in ProfilePage:
     * "Technology" | "Education" | "Healthcare" | "Finance" |
     * "Manufacturing" | "Retail" | "Consulting" | "Government" |
     * "NGO" | "Other"
     */
    private String industry;

    /** replaces old `bio` for Manager role */
    private String description;

    // ── Getters & Setters ─────────────────────────────────────────

    public String getRole()         { return role; }
    public void   setRole(String v) { this.role = v; }

    // Student
    public String getMobileNumber()         { return mobileNumber; }
    public void   setMobileNumber(String v) { this.mobileNumber = v; }

    public String getDateOfBirth()         { return dateOfBirth; }
    public void   setDateOfBirth(String v) { this.dateOfBirth = v; }

    public String getGender()         { return gender; }
    public void   setGender(String v) { this.gender = v; }

    public String getCity()         { return city; }
    public void   setCity(String v) { this.city = v; }

    public String getState()         { return state; }
    public void   setState(String v) { this.state = v; }

    public String getCountry()         { return country; }
    public void   setCountry(String v) { this.country = v; }

    public String getQualification()         { return qualification; }
    public void   setQualification(String v) { this.qualification = v; }

    public String getCollegeName()         { return collegeName; }
    public void   setCollegeName(String v) { this.collegeName = v; }

    public String getYearOfPassing()         { return yearOfPassing; }
    public void   setYearOfPassing(String v) { this.yearOfPassing = v; }

    public String getDomain()         { return domain; }
    public void   setDomain(String v) { this.domain = v; }

    public String getExperience()         { return experience; }
    public void   setExperience(String v) { this.experience = v; }

    // Trainer
    public String getLinkedinUrl()         { return linkedinUrl; }
    public void   setLinkedinUrl(String v) { this.linkedinUrl = v; }

    public String getCourseTopic()         { return courseTopic; }
    public void   setCourseTopic(String v) { this.courseTopic = v; }

    public String getAudienceSize()         { return audienceSize; }
    public void   setAudienceSize(String v) { this.audienceSize = v; }

    public String getFullTimeRole()         { return fullTimeRole; }
    public void   setFullTimeRole(String v) { this.fullTimeRole = v; }

    public List<String> getPlatforms() { return platforms; }
    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }
    // Manager / Business
    public String getOrganizationName()         { return organizationName; }
    public void   setOrganizationName(String v) { this.organizationName = v; }

    public String getWebsiteDomain()         { return websiteDomain; }
    public void   setWebsiteDomain(String v) { this.websiteDomain = v; }

    public String getContactEmail()         { return contactEmail; }
    public void   setContactEmail(String v) { this.contactEmail = v; }

    public String getLocation()         { return location; }
    public void   setLocation(String v) { this.location = v; }

    public String getIndustry()         { return industry; }
    public void   setIndustry(String v) { this.industry = v; }

    public String getDescription()         { return description; }
    public void   setDescription(String v) { this.description = v; }
}