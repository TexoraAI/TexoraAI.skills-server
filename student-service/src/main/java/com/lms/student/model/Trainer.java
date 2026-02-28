//package com.lms.student.model;
//
//import jakarta.persistence.*;
//import java.time.Instant;
//
//@Entity
//@Table(
//    name = "trainers",
//    uniqueConstraints = {
//        @UniqueConstraint(columnNames = "email")
//    }
//)
//public class Trainer {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String name;
//
//    @Column(nullable = false)
//    private String email;
//
//    private String expertise;
//
//    @Column(nullable = false)
//    private String status = "ACTIVE";
//
//    @Column(name = "created_at", nullable = false)
//    private Instant createdAt = Instant.now();
//
//    // getters & setters
//    public Long getId() { return id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getExpertise() { return expertise; }
//    public void setExpertise(String expertise) { this.expertise = expertise; }
//
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//
//    public Instant getCreatedAt() { return createdAt; }
//}




package com.lms.student.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "trainers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "user_id")
    }
)
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔑 authUserId (single identity across system)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    // 📧 display only (not identity)
    @Column(nullable = false)
    private String email;

    private String expertise;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Trainer() {}

    // ---------- getters & setters ----------

    public Long getId() {
        return id;
    }

    public Long getUserId() {           // authUserId
        return userId;
    }

    public void setUserId(Long userId) { // authUserId
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {           // display only
        return email;
    }

    public void setEmail(String email) { // display only
        this.email = email;
    }

    public String getExpertise() {
        return expertise;
    }

    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
