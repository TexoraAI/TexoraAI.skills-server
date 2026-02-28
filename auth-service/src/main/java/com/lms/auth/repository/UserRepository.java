package com.lms.auth.repository;

import com.lms.auth.model.Role;
import com.lms.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // ✅ NEW: Pending users by role
    List<User> findByRoleAndApproved(Role role, boolean approved);

    // ✅ NEW: All users by role
    List<User> findByRole(Role role);
    List<User> findByRoleAndApprovedFalseAndEmailVerifiedTrue(Role role);

}
