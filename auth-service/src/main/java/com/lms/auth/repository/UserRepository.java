
package com.lms.auth.repository;

import com.lms.auth.model.Role;
import com.lms.auth.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    List<User> findByRoleAndApproved(Role role, boolean approved);
    List<User> findByRole(Role role);
    List<User> findByRoleAndApprovedFalseAndEmailVerifiedTrue(Role role);
    List<User> findByOrganizationId(UUID organizationId);
    List<User> findByRoleAndOrganizationId(Role role, UUID organizationId);
    List<User> findByRoleAndApprovedFalseAndOrganizationId(Role role, UUID organizationId);
    List<User> findByCreatedBy(Long createdBy);
    long countByOrganizationIdAndRole(UUID organizationId, Role role);

    // OPTIMIZATION: Replaces findAll() + Java stream filter in getOnboardingResponses().
    // Filters to only STUDENT/TRAINER/TENANT_ADMIN/BUSINESS roles with no org assigned.
    // DB-level sort by created_at DESC. Pageable prevents unbounded result sets.
//    @Query("""
//        SELECT u FROM User u
//        WHERE u.role IN ('STUDENT', 'TRAINER', 'TENANT_ADMIN', 'BUSINESS')
//          AND u.organizationId IS NULL
//        ORDER BY u.createdAt DESC
//        """)
//    List<User> findOnboardingUsers(Pageable pageable);
//
// 
    @Query("""
    	    SELECT u FROM User u
    	    WHERE (u.role IN ('STUDENT', 'TRAINER', 'BUSINESS') AND u.organizationId IS NULL)
    	       OR u.role = 'TENANT_ADMIN'
    	    ORDER BY u.createdAt DESC
    	    """)
    	List<User> findOnboardingUsers(Pageable pageable);
}