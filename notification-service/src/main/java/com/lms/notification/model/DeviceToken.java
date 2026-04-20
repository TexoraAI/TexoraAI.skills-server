// com/lms/notification/model/DeviceToken.java
package com.lms.notification.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "device_tokens")
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "fcm_token", nullable = false, unique = true)
    private String fcmToken;

    // "WEB", "ANDROID", "IOS"
    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();
    
    @Column(name = "user_role")
    private String userRole;

    

    // getters + setters
    public Long getId() { return id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public Instant getCreatedAt() { return createdAt; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
}