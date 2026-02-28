//package com.lms.student.model;
//
//import jakarta.persistence.*;
//import java.time.Instant;
//
//@Entity
//@Table(
//    name = "students",
//    uniqueConstraints = {
//        @UniqueConstraint(columnNames = "user_id")
//    }
//)
//public class Student {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "user_id", nullable = false)
//    private Long userId;
//    
//    @Column(nullable = false, unique = true)
//    private String email;
//
//    @Column(nullable = false)
//    private String status; // ACTIVE, BLOCKED
//
//    @Column(name = "joined_at", nullable = false)
//    private Instant joinedAt = Instant.now();
//
//    @Column(name = "last_active_at")
//    private Instant lastActiveAt;
//
//    public Student() {}
//
//    // getters & setters
//    public Long getId() { return id; }
//
//    public Long getUserId() { return userId; }
//    public void setUserId(Long userId) { this.userId = userId; }
//
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//
//    public Instant getJoinedAt() { return joinedAt; }
//
//    public Instant getLastActiveAt() { return lastActiveAt; }
//    public void setLastActiveAt(Instant lastActiveAt) {
//        this.lastActiveAt = lastActiveAt;
//    }
//    
//    public String getEmail() {           // ✅ NEW
//        return email;
//    }
//
//    public void setEmail(String email) { // ✅ NEW
//        this.email = email;
//    }
//}







package com.lms.student.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "students",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id")
        }
)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔑 authUserId (DO NOT CHANGE COLUMN NAME)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 🔁 display only
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String status; // ACTIVE, BLOCKED

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt = Instant.now();

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    public Student() {}

    // ---------- GETTERS & SETTERS ----------

    public Long getId() {
        return id;
    }

    public Long getUserId() { // authUserId
        return userId;
    }

    public void setUserId(Long userId) { // authUserId
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public Instant getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(Instant lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}
