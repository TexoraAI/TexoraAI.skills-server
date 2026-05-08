//package com.lms.live_session.entity;
//
//import jakarta.persistence.*;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "public_session_bookings")
//public class PublicSessionBooking {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private Long sessionId;           // Reference to LiveSession
//    private String fullName;
//    private String email;
//    private String phoneNumber;
//    private String country;
//    private Boolean gdprConsent;      // Privacy consent
//
//    private LocalDateTime bookedAt;
//    private String bookingStatus;     // "ACTIVE", "ATTENDED", "CANCELLED"
//    private String uniqueAccessToken; // For joining without auth
//    private LocalDateTime joinedAt;
//    private LocalDateTime leftAt;
//
//    public PublicSessionBooking() {}
//
//    public PublicSessionBooking(Long sessionId, String fullName, String email, 
//                               String phoneNumber, String country, Boolean gdprConsent) {
//        this.sessionId = sessionId;
//        this.fullName = fullName;
//        this.email = email;
//        this.phoneNumber = phoneNumber;
//        this.country = country;
//        this.gdprConsent = gdprConsent;
//        this.bookedAt = LocalDateTime.now();
//        this.bookingStatus = "ACTIVE";
//    }
//
//    // ✅ Getters & Setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public Long getSessionId() { return sessionId; }
//    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
//
//    public String getFullName() { return fullName; }
//    public void setFullName(String fullName) { this.fullName = fullName; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getPhoneNumber() { return phoneNumber; }
//    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
//
//    public String getCountry() { return country; }
//    public void setCountry(String country) { this.country = country; }
//
//    public Boolean getGdprConsent() { return gdprConsent; }
//    public void setGdprConsent(Boolean gdprConsent) { this.gdprConsent = gdprConsent; }
//
//    public LocalDateTime getBookedAt() { return bookedAt; }
//    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }
//
//    public String getBookingStatus() { return bookingStatus; }
//    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }
//
//    public String getUniqueAccessToken() { return uniqueAccessToken; }
//    public void setUniqueAccessToken(String uniqueAccessToken) { this.uniqueAccessToken = uniqueAccessToken; }
//
//    public LocalDateTime getJoinedAt() { return joinedAt; }
//    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
//
//    public LocalDateTime getLeftAt() { return leftAt; }
//    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }
//}
package com.lms.live_session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "public_session_bookings")
public class PublicSessionBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String country;
    private Boolean gdprConsent;

    // ✅ NEW CUSTOM FIELDS
    private String topicsOfInterest;   // e.g. "Java, Python, DevOps"
    private String jobRole;            // e.g. "Software Engineer", "Student"
    private String howDidYouHear;      // e.g. "WhatsApp", "LinkedIn", "Friend"
    private String learningGoal;       // e.g. "Job Change", "Upskill", "Fresher"

    @Column(unique = true)
    private String uniqueAccessToken;

    private String bookingStatus = "ACTIVE"; // ACTIVE, ATTENDED, CANCELLED

    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────────────

    public PublicSessionBooking() {}

    // Original constructor (keeps backward compat)
    public PublicSessionBooking(Long sessionId, String fullName, String email,
                                String phoneNumber, String country, Boolean gdprConsent) {
        this.sessionId    = sessionId;
        this.fullName     = fullName;
        this.email        = email;
        this.phoneNumber  = phoneNumber;
        this.country      = country;
        this.gdprConsent  = gdprConsent;
    }

    // ✅ Full constructor with new fields
    public PublicSessionBooking(Long sessionId, String fullName, String email,
                                String phoneNumber, String country, Boolean gdprConsent,
                                String topicsOfInterest, String jobRole,
                                String howDidYouHear, String learningGoal) {
        this.sessionId         = sessionId;
        this.fullName          = fullName;
        this.email             = email;
        this.phoneNumber       = phoneNumber;
        this.country           = country;
        this.gdprConsent       = gdprConsent;
        this.topicsOfInterest  = topicsOfInterest;
        this.jobRole           = jobRole;
        this.howDidYouHear     = howDidYouHear;
        this.learningGoal      = learningGoal;
    }

    // ── Getters & Setters ─────────────────────────────────────────────

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public Boolean getGdprConsent() { return gdprConsent; }
    public void setGdprConsent(Boolean gdprConsent) { this.gdprConsent = gdprConsent; }

    public String getTopicsOfInterest() { return topicsOfInterest; }
    public void setTopicsOfInterest(String topicsOfInterest) { this.topicsOfInterest = topicsOfInterest; }

    public String getJobRole() { return jobRole; }
    public void setJobRole(String jobRole) { this.jobRole = jobRole; }

    public String getHowDidYouHear() { return howDidYouHear; }
    public void setHowDidYouHear(String howDidYouHear) { this.howDidYouHear = howDidYouHear; }

    public String getLearningGoal() { return learningGoal; }
    public void setLearningGoal(String learningGoal) { this.learningGoal = learningGoal; }

    public String getUniqueAccessToken() { return uniqueAccessToken; }
    public void setUniqueAccessToken(String uniqueAccessToken) { this.uniqueAccessToken = uniqueAccessToken; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public LocalDateTime getLeftAt() { return leftAt; }
    public void setLeftAt(LocalDateTime leftAt) { this.leftAt = leftAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}