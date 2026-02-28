//package com.lms.auth.service;
//
//import com.lms.auth.dto.AdminApplyRequest;
//import com.lms.auth.dto.AdminResponse;
//import com.lms.auth.model.AdminProfile;
//import com.lms.auth.model.Role;
//import com.lms.auth.model.User;
//import com.lms.auth.repository.AdminProfileRepository;
//import com.lms.auth.repository.UserRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.List;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//public class AdminService {
//
//    private final UserRepository userRepository;
//    private final AdminProfileRepository adminProfileRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Value("${app.upload.dir:uploads}")
//    private String uploadDir;
//
//    public AdminService(UserRepository userRepository,
//                        AdminProfileRepository adminProfileRepository,
//                        PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.adminProfileRepository = adminProfileRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    // ✅ Apply Admin
//    public AdminResponse apply(AdminApplyRequest request,
//                               MultipartFile idProof,
//                               MultipartFile appointmentLetter) {
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
//        }
//
//        if (idProof == null || idProof.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID Proof file is required");
//        }
//
//        if (appointmentLetter == null || appointmentLetter.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment Letter file is required");
//        }
//
//        // 1) Create User
//        User user = new User();
//        user.setName(request.getFullName());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(Role.ADMIN);
//
//        // pending approval
//        user.setApproved(false);
//
//        User savedUser = userRepository.save(user);
//
//        // 2) Save files
//        String idProofPath = saveFile(idProof);
//        String appointmentLetterPath = saveFile(appointmentLetter);
//
//        // 3) Create AdminProfile
//        AdminProfile profile = new AdminProfile();
//        profile.setUser(savedUser);
//        profile.setFullName(request.getFullName());
//        profile.setMobileNumber(request.getMobileNumber());
//        profile.setAdminType(request.getAdminType());
//        profile.setDepartment(request.getDepartment());
//        profile.setLocation(request.getLocation());
//        profile.setEmployeeId(request.getEmployeeId());
//        profile.setIdProofPath(idProofPath);
//        profile.setAppointmentLetterPath(appointmentLetterPath);
//
//        adminProfileRepository.save(profile);
//
//        return new AdminResponse(savedUser.getId(), profile.getFullName(), savedUser.getEmail(), savedUser.isApproved());
//    }
//
//    // ✅ Pending Admins
//    public List<AdminResponse> getPendingAdmins() {
//
//        List<User> pendingUsers = userRepository.findAll()
//                .stream()
//                .filter(u -> u.getRole() == Role.ADMIN && !u.isApproved())
//                .collect(Collectors.toList());
//
//        return pendingUsers.stream().map(u -> {
//            AdminProfile p = adminProfileRepository.findByUserEmail(u.getEmail()).orElse(null);
//            String name = p != null ? p.getFullName() : u.getName();
//            return new AdminResponse(u.getId(), name, u.getEmail(), u.isApproved());
//        }).collect(Collectors.toList());
//    }
//
//    // ✅ Approve Admin
//    public void approveAdmin(Long userId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
//
//        if (user.getRole() != Role.ADMIN) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not an admin user");
//        }
//
//        user.setApproved(true);
//        userRepository.save(user);
//    }
//
//    // ✅ Reject Admin
//    public void rejectAdmin(Long userId) {
//
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
//
//        if (user.getRole() != Role.ADMIN) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not an admin user");
//        }
//
//        user.setApproved(false);
//        userRepository.save(user);
//    }
//
//    private String saveFile(MultipartFile file) {
//
//        try {
//            File folder = new File(uploadDir);
//            if (!folder.exists()) {
//                folder.mkdirs();
//            }
//
//            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            File destination = new File(folder, fileName);
//
//            file.transferTo(destination);
//
//            return destination.getAbsolutePath();
//        } catch (IOException e) {
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");
//        }
//    }
//}







package com.lms.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.auth.dto.AdminApplyRequest;
import com.lms.auth.dto.AdminResponse;
import com.lms.auth.event.AuthEvent;
import com.lms.auth.model.AdminProfile;
import com.lms.auth.model.Role;
import com.lms.auth.model.User;
import com.lms.auth.producer.AuthEventProducer;
import com.lms.auth.repository.AdminProfileRepository;
import com.lms.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ ONLY ADDED (Kafka producer)
    private final AuthEventProducer authEventProducer;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    // ❌ constructor logic NOT changed — ONLY parameter added
    public AdminService(UserRepository userRepository,
                        AdminProfileRepository adminProfileRepository,
                        PasswordEncoder passwordEncoder,
                        AuthEventProducer authEventProducer) {

        this.userRepository = userRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authEventProducer = authEventProducer;
    }

    // ✅ Apply Admin (UNCHANGED)
    public AdminResponse apply(AdminApplyRequest request,
                               MultipartFile idProof,
                               MultipartFile appointmentLetter) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        if (idProof == null || idProof.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID Proof file is required");
        }

        if (appointmentLetter == null || appointmentLetter.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment Letter file is required");
        }

        User user = new User();
        user.setName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user.setApproved(false);

        User savedUser = userRepository.save(user);

        String idProofPath = saveFile(idProof);
        String appointmentLetterPath = saveFile(appointmentLetter);

        AdminProfile profile = new AdminProfile();
        profile.setUser(savedUser);
        profile.setFullName(request.getFullName());
        profile.setMobileNumber(request.getMobileNumber());
        profile.setAdminType(request.getAdminType());
        profile.setDepartment(request.getDepartment());
        profile.setLocation(request.getLocation());
        profile.setEmployeeId(request.getEmployeeId());
        profile.setIdProofPath(idProofPath);
        profile.setAppointmentLetterPath(appointmentLetterPath);

        adminProfileRepository.save(profile);

        return new AdminResponse(
                savedUser.getId(),
                profile.getFullName(),
                savedUser.getEmail(),
                savedUser.isApproved()
        );
    }

    // ✅ Pending Admins (UNCHANGED)
    public List<AdminResponse> getPendingAdmins() {

        List<User> pendingUsers = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == Role.ADMIN && !u.isApproved())
                .collect(Collectors.toList());

        return pendingUsers.stream().map(u -> {
            AdminProfile p = adminProfileRepository
                    .findByUserEmail(u.getEmail()).orElse(null);
            String name = p != null ? p.getFullName() : u.getName();
            return new AdminResponse(u.getId(), name, u.getEmail(), u.isApproved());
        }).collect(Collectors.toList());
    }

    // ✅ Approve Admin (ONLY KAFKA ADDED)
    public void approveAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found")
                );

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not an admin user");
        }

        user.setApproved(true);
        userRepository.save(user);

        // 🔥 ONLY MISSING PART (Kafka)
        try {
            AuthEvent event = new AuthEvent();
            event.setEventType("USER_CREATED");
            event.setUserId(user.getId());
            event.setEmail(user.getEmail());
            event.setRole("ADMIN");
            event.setDisplayName(user.getName());

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(event);

            authEventProducer.sendEvent(event);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Reject Admin (UNCHANGED)
    public void rejectAdmin(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found")
                );

        if (user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not an admin user");
        }

        user.setApproved(false);
        userRepository.save(user);
    }

    private String saveFile(MultipartFile file) {

        try {
            File folder = new File(uploadDir);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File destination = new File(folder, fileName);
            file.transferTo(destination);
            return destination.getAbsolutePath();

        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed"
            );
        }
    }
}


