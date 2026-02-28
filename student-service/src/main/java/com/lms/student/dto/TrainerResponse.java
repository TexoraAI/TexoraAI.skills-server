//package com.lms.student.dto;
//
//import java.time.Instant;
//
//public class TrainerResponse {
//
//    private Long id;
//    private String name;
//    private String email;
//    private String expertise;
//    private String status;
//    private Instant createdAt;
//
//    // getters & setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
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
//    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
//}



package com.lms.student.dto;

import java.time.Instant;

public class TrainerResponse {

    private Long id;

    // 🔑 authUserId (REQUIRED)
    private Long userId;

    private String name;
    private String email;
    private String expertise;
    private String status;
    private Instant createdAt;

    // -------- GETTERS & SETTERS --------

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {   // ✅ FIX
        return userId;
    }

    public void setUserId(Long userId) { // ✅ FIX
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
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

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
