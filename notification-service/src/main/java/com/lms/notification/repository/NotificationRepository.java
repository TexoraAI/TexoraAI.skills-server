//package com.lms.notification.repository;
//import com.lms.notification.model.Notification;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.List;
//
//public interface NotificationRepository extends JpaRepository<Notification, Long> {
//    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
//    long countByUserIdAndReadFalse(String userId);
//
//    @Modifying @Transactional
//    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
//    void markAllReadByUserId(String userId);
//    void deleteAllByUserId(String userId);
//}

package com.lms.notification.repository;
import com.lms.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // ── Existing (for Student & Trainer) ──
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    long countByUserIdAndReadFalse(String userId);

    @Modifying @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
    void markAllReadByUserId(String userId);
    
    void deleteAllByUserId(String userId);

    // ✅ NEW (for Admin role-based notifications) ──
    List<Notification> findByUserRoleOrderByCreatedAtDesc(String userRole);
    
    long countByUserRoleAndReadFalse(String userRole);

    @Modifying @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.userRole = :userRole")
    void markAllReadByUserRole(String userRole);

    @Modifying @Transactional
    @Query("DELETE FROM Notification n WHERE n.userRole = :userRole")
    void deleteAllByUserRole(String userRole);
    
    
    
    
}