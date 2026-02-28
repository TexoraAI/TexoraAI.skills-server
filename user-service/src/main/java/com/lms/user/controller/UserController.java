


package com.lms.user.controller;

import com.lms.user.dto.CreateUserRequest;
import com.lms.user.dto.UpdateUserRequest;
import com.lms.user.dto.UserResponse;
import com.lms.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // =====================================================
    // ✅ 1A) GET LOGGED-IN USER PROFILE
    // =====================================================
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Authentication is null or username missing");
        }

        String email = auth.getName();
        return ResponseEntity.ok(service.getByEmail(email));
    }

    // =====================================================
    // ✅ 1B) UPDATE LOGGED-IN USER PROFILE
    // =====================================================
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(@RequestBody UpdateUserRequest req) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Authentication is null or username missing");
        }

        String email = auth.getName();
        return ResponseEntity.ok(service.updateByEmail(email, req));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(service.createUser(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(service.updateUser(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

 // =====================================================
 // ✅ NEW API — GET USER BY EMAIL (FOR OTHER MICROSERVICES)
 // =====================================================
 @GetMapping("/email/{email}")
 public ResponseEntity<UserResponse> getByEmail(@PathVariable String email) {
     return ResponseEntity.ok(service.getByEmail(email));
 }

 /* =====================================================
 INTERNAL API — FOR BATCH SERVICE ONLY
 ===================================================== */
//================= INTERNAL APIs FOR MICROSERVICES =================

//trainers list
@GetMapping("/internal/trainers")
public ResponseEntity<List<UserResponse>> getAllTrainers() {
  return ResponseEntity.ok(service.getUsersByRole("TRAINER"));
}

//students list
@GetMapping("/internal/students")
public ResponseEntity<List<UserResponse>> getAllStudents() {
  return ResponseEntity.ok(service.getUsersByRole("STUDENT"));
}


    
    
    @GetMapping
    public ResponseEntity<Page<UserResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        return ResponseEntity.ok(service.listUsers(page, size, sort, dir));
    }
}
