//
//package com.lms.user.repo;
//
//import com.lms.user.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface UserRepository extends JpaRepository<User, Long> {
//
//    Optional<User> findByEmail(String email);
//
//    boolean existsByEmail(String email);
//
//    // Existing: find users by a specific role substring
//    @Query("SELECT u FROM User u WHERE u.roles LIKE %:role%")
//    List<User> findUsersByRole(@Param("role") String role);
//
//    // ← NEW: find all users belonging to a specific organization
//    List<User> findByOrganizationId(String organizationId);
//
//    // ← NEW: find users by org AND role (e.g. get only STUDENT users in an org)
//    @Query("SELECT u FROM User u WHERE u.organizationId = :orgId AND u.roles LIKE %:role%")
//    List<User> findByOrganizationIdAndRolesContaining(
//            @Param("orgId") String organizationId,
//            @Param("role")  String role
//    );
//}

package com.lms.user.repo;

import com.lms.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// WHY: Primary data access layer for all LMS user operations across services
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // WHY: Login and JWT validation look up user by email — most frequent DB call
    Optional<User> findByEmail(String email);

    // WHY: Registration guard — prevents duplicate accounts in LMS
    boolean existsByEmail(String email);

    // WHY: Admin dashboard filters users by role (STUDENT, TRAINER, ADMIN)
    // OPTIMIZATION: Added Pageable overload to prevent full table scan result load
    @Query("SELECT u FROM User u WHERE u.roles LIKE %:role%")
    Page<User> findUsersByRole(@Param("role") String role, Pageable pageable);

    // WHY: Keep non-paginated version for backward compatibility with internal callers
    @Query("SELECT u FROM User u WHERE u.roles LIKE %:role%")
    List<User> findUsersByRole(@Param("role") String role);

    // WHY: SuperAdmin views users scoped to a specific organization
    // OPTIMIZATION: Added Pageable overload — orgs can have thousands of members
    Page<User> findByOrganizationId(String organizationId, Pageable pageable);

    // WHY: Keep non-paginated for small internal use cases
    List<User> findByOrganizationId(String organizationId);

    // WHY: TenantAdmin needs students-only or trainers-only list within their org
    // OPTIMIZATION: Added Pageable
    @Query("SELECT u FROM User u WHERE u.organizationId = :orgId AND u.roles LIKE %:role%")
    Page<User> findByOrganizationIdAndRolesContaining(
            @Param("orgId") String organizationId,
            @Param("role") String role,
            Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.organizationId = :orgId AND u.roles LIKE %:role%")
    List<User> findByOrganizationIdAndRolesContaining(
            @Param("orgId") String organizationId,
            @Param("role") String role);
    
    Page<User> findByOrganizationIdIsNullAndRolesContaining(String role, Pageable pageable);
}