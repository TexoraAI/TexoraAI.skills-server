//package com.lms.auth.service;
//
//import com.lms.auth.dto.TrainerApplyRequest;
//import com.lms.auth.dto.TrainerResponse;
//import com.lms.auth.model.Role;
//import com.lms.auth.model.TrainerProfile;
//import com.lms.auth.model.User;
//import com.lms.auth.repository.TrainerProfileRepository;
//import com.lms.auth.repository.UserRepository;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//public class TrainerService {
//
//    private final UserRepository userRepository;
//    private final TrainerProfileRepository trainerProfileRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public TrainerService(UserRepository userRepository,
//                          TrainerProfileRepository trainerProfileRepository,
//                          PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.trainerProfileRepository = trainerProfileRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    // ✅ Trainer Apply
//    public TrainerResponse apply(TrainerApplyRequest request) {
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
//        }
//
//        // 1) Create User (pending)
//        User user = new User();
//        user.setName(request.getFullName());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(Role.TRAINER);
//
//        // pending approval
//        user.setApproved(false);
//
//        User savedUser = userRepository.save(user);
//
//        // 2) Create TrainerProfile
//        TrainerProfile profile = new TrainerProfile();
//        profile.setUser(savedUser);
//        profile.setFullName(request.getFullName());
//        profile.setEmail(request.getEmail());
//        profile.setLinkedinUrl(request.getLinkedinUrl());
//        profile.setCountry(request.getCountry());
//        profile.setPlatforms(request.getPlatforms());
//        profile.setAudienceSize(request.getAudienceSize());
//        profile.setFullTimeRole(request.getFullTimeRole());
//        profile.setCourseTopic(request.getCourseTopic());
//
//        trainerProfileRepository.save(profile);
//
//        return new TrainerResponse(
//                savedUser.getId(),
//                profile.getFullName(),
//                savedUser.getEmail(),
//                savedUser.isApproved()
//        );
//    }
//
//    // ✅ Admin: list pending trainers
//    public List<TrainerResponse> getPendingTrainers() {
//
//        List<User> pendingUsers = userRepository.findAll()
//                .stream()
//                .filter(u -> u.getRole() == Role.TRAINER && !u.isApproved())
//                .collect(Collectors.toList());
//
//        return pendingUsers.stream().map(u -> {
//            TrainerProfile p = trainerProfileRepository.findByEmail(u.getEmail()).orElse(null);
//            String fullName = p != null ? p.getFullName() : u.getName();
//            return new TrainerResponse(u.getId(), fullName, u.getEmail(), u.isApproved());
//        }).collect(Collectors.toList());
//    }
//
//    // ✅ Admin: approve trainer
//    public void approveTrainer(Long userId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found"));
//
//        if (user.getRole() != Role.TRAINER) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a trainer user");
//        }
//
//        user.setApproved(true);
//        userRepository.save(user);
//    }
//
//    // ✅ Admin: reject trainer (optional)
//    public void rejectTrainer(Long userId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found"));
//
//        if (user.getRole() != Role.TRAINER) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a trainer user");
//        }
//
//        // easiest rejection: delete profile + user
//        trainerProfileRepository.findByEmail(user.getEmail()).ifPresent(trainerProfileRepository::delete);
//        userRepository.delete(user);
//    }
//}







package com.lms.auth.service;

import com.lms.auth.dto.TrainerApplyRequest;
import com.lms.auth.dto.TrainerResponse;
import com.lms.auth.event.AuthEvent;                    // ✅ ADDED
import com.lms.auth.model.Role;
import com.lms.auth.model.TrainerProfile;
import com.lms.auth.model.User;
import com.lms.auth.producer.AuthEventProducer;         // ✅ ADDED
import com.lms.auth.repository.TrainerProfileRepository;
import com.lms.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrainerService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ ADDED (Kafka Producer)
    private final AuthEventProducer authEventProducer;

    // ❌ constructor logic NOT changed — ONLY parameter added
    public TrainerService(UserRepository userRepository,
                          TrainerProfileRepository trainerProfileRepository,
                          PasswordEncoder passwordEncoder,
                          AuthEventProducer authEventProducer) {

        this.userRepository = userRepository;
        this.trainerProfileRepository = trainerProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authEventProducer = authEventProducer;
    }

    // ✅ Trainer Apply — UNCHANGED
    public TrainerResponse apply(TrainerApplyRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.TRAINER);
        user.setApproved(false);

        User savedUser = userRepository.save(user);

        TrainerProfile profile = new TrainerProfile();
        profile.setUser(savedUser);
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setCountry(request.getCountry());
        profile.setPlatforms(request.getPlatforms());
        profile.setAudienceSize(request.getAudienceSize());
        profile.setFullTimeRole(request.getFullTimeRole());
        profile.setCourseTopic(request.getCourseTopic());

        trainerProfileRepository.save(profile);

        return new TrainerResponse(
                savedUser.getId(),
                profile.getFullName(),
                savedUser.getEmail(),
                savedUser.isApproved()
        );
    }

    // ✅ Admin: list pending trainers — UNCHANGED
    public List<TrainerResponse> getPendingTrainers() {

        List<User> pendingUsers = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == Role.TRAINER && !u.isApproved())
                .collect(Collectors.toList());

        return pendingUsers.stream().map(u -> {
            TrainerProfile p = trainerProfileRepository.findByEmail(u.getEmail()).orElse(null);
            String fullName = p != null ? p.getFullName() : u.getName();
            return new TrainerResponse(u.getId(), fullName, u.getEmail(), u.isApproved());
        }).collect(Collectors.toList());
    }

    // ✅ Admin: approve trainer — ONLY KAFKA ADDED
    public void approveTrainer(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Trainer not found"
                        )
                );

        if (user.getRole() != Role.TRAINER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Not a trainer user"
            );
        }

        user.setApproved(true);
        userRepository.save(user);

        // 🔥 ONLY MISSING PART (Kafka)
        AuthEvent event = new AuthEvent();
        event.setEventType("USER_CREATED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setRole("TRAINER");
        event.setDisplayName(user.getName());

        authEventProducer.sendEvent(event);
    }

    // ✅ Admin: reject trainer — UNCHANGED
    public void rejectTrainer(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Trainer not found"
                        )
                );

        if (user.getRole() != Role.TRAINER) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Not a trainer user"
            );
        }

        trainerProfileRepository.findByEmail(user.getEmail())
                .ifPresent(trainerProfileRepository::delete);

        userRepository.delete(user);
    }
}
