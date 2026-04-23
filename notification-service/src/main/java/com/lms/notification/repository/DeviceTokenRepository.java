//// com/lms/notification/repository/DeviceTokenRepository.java
package com.lms.notification.repository;

import com.lms.notification.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    List<DeviceToken> findByUserEmail(String userEmail);
    Optional<DeviceToken> findByFcmToken(String fcmToken);
    void deleteByFcmToken(String fcmToken);
    List<DeviceToken> findByUserRole(String userRole);
}
//
//package com.lms.notification.repository;
//
//import com.lms.notification.model.DeviceToken;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
//
//    // original — kept for backward compat
//    List<DeviceToken> findByUserEmail(String userEmail);
//
//    // ✅ NEW — case-insensitive email lookup
//    List<DeviceToken> findByUserEmailIgnoreCase(String userEmail);
//
//    Optional<DeviceToken> findByFcmToken(String fcmToken);
//
//    void deleteByFcmToken(String fcmToken);
//
//    // original — kept for backward compat
//    List<DeviceToken> findByUserRole(String userRole);
//
//    // ✅ NEW — case-insensitive role lookup
//    List<DeviceToken> findByUserRoleIgnoreCase(String userRole);
//}