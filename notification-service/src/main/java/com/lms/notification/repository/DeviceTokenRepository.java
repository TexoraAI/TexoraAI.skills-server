// com/lms/notification/repository/DeviceTokenRepository.java
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