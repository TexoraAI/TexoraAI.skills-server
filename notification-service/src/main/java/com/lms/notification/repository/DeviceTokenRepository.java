////// com/lms/notification/repository/DeviceTokenRepository.java
//package com.lms.notification.repository;
//
//import com.lms.notification.model.DeviceToken;
//import org.springframework.data.jpa.repository.JpaRepository;
//import java.util.List;
//import java.util.Optional;
//
//public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
//    List<DeviceToken> findByUserEmail(String userEmail);
//    Optional<DeviceToken> findByFcmToken(String fcmToken);
//    void deleteByFcmToken(String fcmToken);
//    List<DeviceToken> findByUserRole(String userRole);
//}



package com.lms.notification.repository;

import com.lms.notification.model.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    List<DeviceToken> findByUserEmail(String userEmail);

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    void deleteByFcmToken(String fcmToken);

    List<DeviceToken> findByUserRole(String userRole);

    // ✅ FIX: case-insensitive email lookup using JPQL
    @Query("SELECT d FROM DeviceToken d WHERE LOWER(d.userEmail) = LOWER(:email)")
    List<DeviceToken> findByUserEmailCaseInsensitive(@Param("email") String email);

    // ✅ FIX: case-insensitive role lookup using JPQL
    @Query("SELECT d FROM DeviceToken d WHERE UPPER(d.userRole) = UPPER(:role)")
    List<DeviceToken> findByUserRoleCaseInsensitive(@Param("role") String role);
}