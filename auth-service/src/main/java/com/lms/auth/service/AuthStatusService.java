package com.lms.auth.service;

import com.lms.auth.dto.UserApprovalStatusResponse;
import com.lms.auth.model.User;
import com.lms.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthStatusService {

    private final UserRepository userRepository;

    public AuthStatusService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserApprovalStatusResponse getStatusByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // status logic
        String status;

        // ✅ If rejected -> approved = false and emailVerified = true
        // (if you want strict reject tracking later, we can add "rejected" column)
        if (!user.isApproved()) {
            status = "PENDING";
        } else {
            status = "APPROVED";
        }

        return new UserApprovalStatusResponse(
                user.getEmail(),
                user.getRole().name(),
                status
        );
    }
}
