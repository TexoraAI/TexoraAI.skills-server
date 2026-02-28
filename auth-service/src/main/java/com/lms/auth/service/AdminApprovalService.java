



package com.lms.auth.service;

import com.lms.auth.dto.PendingUserResponse;
import com.lms.auth.event.AuthEvent;
import com.lms.auth.model.Role;
import com.lms.auth.model.User;
import com.lms.auth.producer.AuthEventProducer;
import com.lms.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminApprovalService {

    private final UserRepository userRepository;
    private final AuthEventProducer authEventProducer;

    public AdminApprovalService(UserRepository userRepository,
                                AuthEventProducer authEventProducer) {
        this.userRepository = userRepository;
        this.authEventProducer = authEventProducer;
    }

    // ================= GET PENDING =================
    public List<PendingUserResponse> getPending(Role role) {
        return userRepository
                .findByRoleAndApprovedFalseAndEmailVerifiedTrue(role)
                .stream()
                .map(u -> new PendingUserResponse(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole().name(),
                        u.isApproved()
                ))
                .toList();
    }

    // ================= APPROVE =================
    public void approve(Long userId, Role role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != role) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role mismatch");
        }

        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email not verified"
            );
        }

        user.setApproved(true);
        userRepository.save(user);

        // 🔥 USER BECOMES ACTIVE → FIRE EVENT
        authEventProducer.sendEvent(
        	    new AuthEvent(
        	        "USER_CREATED",
        	        user.getId(),              // userId ✅
        	        user.getEmail(),           // email ✅
        	        user.getRole().name(),     // role = STUDENT ✅
        	        user.getName()             // displayName ✅
        	    )
        	);

        System.out.println("✅ USER_APPROVED & EVENT SENT → " + user.getEmail());
    }

    // ================= REJECT =================
    public void reject(Long userId, Role role) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getRole() != role) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role mismatch");
        }

        userRepository.delete(user);

        authEventProducer.sendEvent(
        	    new AuthEvent(
        	        "USER_CREATED",
        	        user.getId(),              // userId ✅
        	        user.getEmail(),           // email ✅
        	        user.getRole().name(),     // role = STUDENT ✅
        	        user.getName()             // displayName ✅
        	    )
        	);


        System.out.println("❌ USER_REJECTED & EVENT SENT → " + user.getEmail());
    }
}
