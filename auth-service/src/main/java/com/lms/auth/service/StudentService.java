//package com.lms.auth.service;
//
//import com.lms.auth.dto.StudentApplyRequest;
//import com.lms.auth.dto.StudentResponse;
//import com.lms.auth.model.Role;
//import com.lms.auth.model.StudentProfile;
//import com.lms.auth.model.User;
//import com.lms.auth.repository.StudentProfileRepository;
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
//public class StudentService {
//
//    private final UserRepository userRepository;
//    private final StudentProfileRepository studentProfileRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public StudentService(UserRepository userRepository,
//                          StudentProfileRepository studentProfileRepository,
//                          PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.studentProfileRepository = studentProfileRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    // ✅ Student Apply (pending)
//    public StudentResponse apply(StudentApplyRequest request) {
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
//        }
//
//        // 1) Create User
//        User user = new User();
//        user.setName(request.getFullName());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(Role.STUDENT);
//
//        // pending approval
//        user.setApproved(false);
//
//        User savedUser = userRepository.save(user);
//
//        // 2) Create Student Profile
//        StudentProfile profile = new StudentProfile();
//        profile.setUser(savedUser);
//        profile.setFullName(request.getFullName());
//        profile.setMobileNumber(request.getMobileNumber());
//        profile.setDateOfBirth(request.getDateOfBirth());
//        profile.setGender(request.getGender());
//        profile.setCity(request.getCity());
//        profile.setState(request.getState());
//        profile.setCountry(request.getCountry());
//        profile.setQualification(request.getQualification());
//        profile.setCollegeName(request.getCollegeName());
//        profile.setYearOfPassing(request.getYearOfPassing());
//        profile.setDomain(request.getDomain());
//        profile.setExperience(request.getExperience());
//
//        studentProfileRepository.save(profile);
//
//        return new StudentResponse(
//                savedUser.getId(),
//                profile.getFullName(),
//                savedUser.getEmail(),
//                savedUser.isApproved()
//        );
//    }
//
//    // ✅ Admin: pending students
//    public List<StudentResponse> getPendingStudents() {
//
//        List<User> pendingUsers = userRepository.findAll()
//                .stream()
//                .filter(u -> u.getRole() == Role.STUDENT && !u.isApproved())
//                .collect(Collectors.toList());
//
//        return pendingUsers.stream().map(u -> {
//            StudentProfile profile = studentProfileRepository.findByUserEmail(u.getEmail()).orElse(null);
//            String fullName = profile != null ? profile.getFullName() : u.getName();
//
//            return new StudentResponse(u.getId(), fullName, u.getEmail(), u.isApproved());
//        }).collect(Collectors.toList());
//    }
//
//    // ✅ Admin: approve
//    public void approveStudent(Long userId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
//
//        if (user.getRole() != Role.STUDENT) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a student user");
//        }
//
//        user.setApproved(true);
//        userRepository.save(user);
//    }
//
//    // ✅ Admin: reject (delete)
//    public void rejectStudent(Long userId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
//
//        if (user.getRole() != Role.STUDENT) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a student user");
//        }
//
//        // delete profile first (optional)
//        studentProfileRepository.findByUserEmail(user.getEmail())
//                .ifPresent(studentProfileRepository::delete);
//
//        userRepository.delete(user);
//    }
//
//    // ✅ Student status
//    public boolean getStatus(String email) {
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
//
//        if (user.getRole() != Role.STUDENT) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a student account");
//        }
//
//        return user.isApproved();
//    }
//}







package com.lms.auth.service;

import com.lms.auth.dto.StudentApplyRequest;
import com.lms.auth.dto.StudentResponse;
import com.lms.auth.event.AuthEvent;                 // ✅ ADDED
import com.lms.auth.model.Role;
import com.lms.auth.model.StudentProfile;
import com.lms.auth.model.User;
import com.lms.auth.producer.AuthEventProducer;      // ✅ ADDED
import com.lms.auth.repository.StudentProfileRepository;
import com.lms.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ ADDED (Kafka Producer)
    private final AuthEventProducer authEventProducer;

    // ❌ constructor logic NOT changed — ONLY parameter added
    public StudentService(UserRepository userRepository,
                          StudentProfileRepository studentProfileRepository,
                          PasswordEncoder passwordEncoder,
                          AuthEventProducer authEventProducer) {

        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authEventProducer = authEventProducer;
    }

    // ✅ Student Apply (pending) — UNCHANGED
    public StudentResponse apply(StudentApplyRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setApproved(false);

        User savedUser = userRepository.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setUser(savedUser);
        profile.setFullName(request.getFullName());
        profile.setMobileNumber(request.getMobileNumber());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setGender(request.getGender());
        profile.setCity(request.getCity());
        profile.setState(request.getState());
        profile.setCountry(request.getCountry());
        profile.setQualification(request.getQualification());
        profile.setCollegeName(request.getCollegeName());
        profile.setYearOfPassing(request.getYearOfPassing());
        profile.setDomain(request.getDomain());
        profile.setExperience(request.getExperience());

        studentProfileRepository.save(profile);

        return new StudentResponse(
                savedUser.getId(),
                profile.getFullName(),
                savedUser.getEmail(),
                savedUser.isApproved()
        );
    }

    // ✅ Admin: pending students — UNCHANGED
    public List<StudentResponse> getPendingStudents() {

        List<User> pendingUsers = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == Role.STUDENT && !u.isApproved())
                .collect(Collectors.toList());

        return pendingUsers.stream().map(u -> {
            StudentProfile profile =
                    studentProfileRepository.findByUserEmail(u.getEmail()).orElse(null);

            String fullName = profile != null ? profile.getFullName() : u.getName();

            return new StudentResponse(
                    u.getId(),
                    fullName,
                    u.getEmail(),
                    u.isApproved()
            );
        }).collect(Collectors.toList());
    }

    // ✅ Admin: approve — ONLY KAFKA ADDED
    public void approveStudent(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Student not found"
                        )
                );

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Not a student user"
            );
        }

        user.setApproved(true);
        userRepository.save(user);

        // 🔥 ONLY MISSING PART (Kafka)
        AuthEvent event = new AuthEvent();
        event.setEventType("USER_CREATED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setRole("STUDENT");
        event.setDisplayName(user.getName());

        authEventProducer.sendEvent(event);
    }

    // ✅ Admin: reject (delete) — UNCHANGED
    public void rejectStudent(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Student not found"
                        )
                );

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Not a student user"
            );
        }

        studentProfileRepository.findByUserEmail(user.getEmail())
                .ifPresent(studentProfileRepository::delete);

        userRepository.delete(user);
    }

    // ✅ Student status — UNCHANGED
    public boolean getStatus(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Student not found"
                        )
                );

        if (user.getRole() != Role.STUDENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Not a student account"
            );
        }

        return user.isApproved();
    }
}
