//   
//package com.lms.user.service;
//
//import com.lms.user.dto.CreateUserRequest;
//import com.lms.user.dto.UpdateUserRequest;
//import com.lms.user.dto.UserResponse;
//import com.lms.user.event.UserEvent;
//import com.lms.user.exception.ResourceNotFoundException;
//import com.lms.user.kafka.UserSyncEventProducer;
//import com.lms.user.model.User;
//import com.lms.user.repo.UserRepository;
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class UserService {
//
//    private final UserRepository repo;
//    private final UserSyncEventProducer producer;
//
//    public UserService(UserRepository repo, UserSyncEventProducer producer) {
//        this.repo = repo;
//        this.producer = producer;
//    }
//
//    // ----------------------------------------------------
//    // CREATE USER (handled via Auth → Kafka)
//    // ----------------------------------------------------
//    @CacheEvict(value = "users", allEntries = true)
//    public UserResponse createUser(CreateUserRequest req) {
//
//        Optional<User> exists = repo.findByEmail(req.getEmail());
//        if (exists.isPresent()) {
//            throw new IllegalArgumentException("Email already exists");
//        }
//
//        User u = new User();
//        u.setEmail(req.getEmail());
//        u.setDisplayName(req.getDisplayName());
//        u.setTenantId(req.getTenantId());
//        u.setRoles(req.getRoles());
//
//        User saved = repo.save(u);
//        return mapToResponse(saved);
//    }
//
//    // ----------------------------------------------------
//    // GET USER BY ID
//    // ----------------------------------------------------
//    @Cacheable(value = "users", key = "#id")
//    public UserResponse getById(Long id) {
//        return repo.findById(id)
//                .map(this::mapToResponse)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("User not found with id " + id));
//    }
//
//    // ----------------------------------------------------
//    // UPDATE USER
//    // ----------------------------------------------------
//    @CacheEvict(value = "users", key = "#id")
//    public UserResponse updateUser(Long id, UpdateUserRequest req) {
//
//        User u = repo.findById(id)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("User not found with id " + id));
//
//        boolean roleChanged = false;
//
//        if (req.getDisplayName() != null) {
//            u.setDisplayName(req.getDisplayName());
//        }
//
//        if (req.getRoles() != null && !req.getRoles().equals(u.getRoles())) {
//            u.setRoles(req.getRoles());
//            roleChanged = true;
//        }
//
//        User saved = repo.save(u);
//
//        // 🔥 USER_UPDATED
//        producer.send(new UserEvent(
//                "USER_UPDATED",
//                saved.getEmail(),
//                saved.getDisplayName(),
//                null
//        ));
//
//        // 🔥 USER_ROLE_CHANGED
//        if (roleChanged) {
//            producer.send(new UserEvent(
//                    "USER_ROLE_CHANGED",
//                    saved.getEmail(),
//                    null,
//                    saved.getRoles().replace("ROLE_", "")
//            ));
//        }
//
//        return mapToResponse(saved);
//    }
//
//    // ----------------------------------------------------
//    // DELETE USER
//    // ----------------------------------------------------
//    @CacheEvict(value = "users", key = "#id")
//    public void deleteUser(Long id) {
//
//        User user = repo.findById(id)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException("User not found with id " + id));
//
//        repo.delete(user);
//
//        producer.send(new UserEvent(
//                "USER_DELETED",
//                user.getEmail(),
//                null,
//                null
//        ));
//    }
//
//    // ----------------------------------------------------
//    // LIST USERS
//    // ----------------------------------------------------
//    public Page<UserResponse> listUsers(int page, int size, String sort, String dir) {
//
//        Sort.Direction direction =
//                "desc".equalsIgnoreCase(dir)
//                        ? Sort.Direction.DESC
//                        : Sort.Direction.ASC;
//
//        Sort s = Sort.by(direction,
//                (sort == null || sort.isEmpty()) ? "id" : sort);
//
//        Pageable p = PageRequest.of(page, size, s);
//        return repo.findAll(p).map(this::mapToResponse);
//    }
//
//    // ----------------------------------------------------
//    // GET BY EMAIL
//    // ----------------------------------------------------
//    @Cacheable(value = "users", key = "#email")
//    public UserResponse getByEmail(String email) {
//
//        User user = repo.findByEmail(email)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "User not found with email " + email
//                        ));
//
//        return mapToResponse(user);
//    }
//
//    // ----------------------------------------------------
//    // UPDATE BY EMAIL
//    // ----------------------------------------------------
//    @CacheEvict(value = "users", allEntries = true)
//    public UserResponse updateByEmail(String email, UpdateUserRequest req) {
//
//        User u = repo.findByEmail(email)
//                .orElseThrow(() ->
//                        new ResourceNotFoundException(
//                                "User not found with email " + email
//                        ));
//
//        boolean roleChanged = false;
//
//        if (req.getDisplayName() != null) {
//            u.setDisplayName(req.getDisplayName());
//        }
//
//        if (req.getRoles() != null && !req.getRoles().equals(u.getRoles())) {
//            u.setRoles(req.getRoles());
//            roleChanged = true;
//        }
//
//        User saved = repo.save(u);
//
//        producer.send(new UserEvent(
//                "USER_UPDATED",
//                saved.getEmail(),
//                saved.getDisplayName(),
//                null
//        ));
//
//        if (roleChanged) {
//            producer.send(new UserEvent(
//                    "USER_ROLE_CHANGED",
//                    saved.getEmail(),
//                    null,
//                    saved.getRoles().replace("ROLE_", "")
//            ));
//        }
//
//        return mapToResponse(saved);
//    }
//
//    // ----------------------------------------------------
//    // MAP ENTITY → RESPONSE
//    // ----------------------------------------------------
//    private UserResponse mapToResponse(User u) {
//        UserResponse r = new UserResponse();
//        r.setId(u.getId());
//        r.setEmail(u.getEmail());
//        r.setDisplayName(u.getDisplayName());
//        r.setRoles(u.getRoles());
//        r.setTenantId(u.getTenantId());
//        r.setCreatedAt(u.getCreatedAt());
//        return r;
//    }
//}





package com.lms.user.service;

import com.lms.user.dto.CreateUserRequest;
import com.lms.user.dto.UpdateUserRequest;
import com.lms.user.dto.UserResponse;
import com.lms.user.event.UserEvent;
import com.lms.user.exception.ResourceNotFoundException;
import com.lms.user.kafka.UserSyncEventProducer;
import com.lms.user.model.User;
import com.lms.user.repo.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final UserSyncEventProducer producer;

    public UserService(UserRepository repo, UserSyncEventProducer producer) {
        this.repo = repo;
        this.producer = producer;
    }

    // ----------------------------------------------------
    // CREATE USER
    // ----------------------------------------------------
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse createUser(CreateUserRequest req) {

        Optional<User> exists = repo.findByEmail(req.getEmail());
        if (exists.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User u = new User();
        u.setEmail(req.getEmail());
        u.setDisplayName(req.getDisplayName());
        u.setTenantId(req.getTenantId());
        u.setRoles(req.getRoles());

        User saved = repo.save(u);
        return mapToResponse(saved);
    }

    // ----------------------------------------------------
    // GET USER BY ID
    // ----------------------------------------------------
    @Cacheable(value = "users", key = "#id")
    public UserResponse getById(Long id) {
        return repo.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id " + id));
    }

    // ----------------------------------------------------
    // UPDATE USER
    // ----------------------------------------------------
    @CacheEvict(value = "users", key = "#id")
    public UserResponse updateUser(Long id, UpdateUserRequest req) {

        User u = repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id " + id));

        boolean roleChanged = false;

        if (req.getDisplayName() != null) {
            u.setDisplayName(req.getDisplayName());
        }

        if (req.getRoles() != null && !req.getRoles().equals(u.getRoles())) {
            u.setRoles(req.getRoles());
            roleChanged = true;
        }

        User saved = repo.save(u);

        // 🔥 USER_UPDATED EVENT
        UserEvent updatedEvent = new UserEvent(
                "USER_UPDATED",
                saved.getEmail(),
                saved.getDisplayName(),
                null
        );
        updatedEvent.setUserId(saved.getId()); // ✅ ONLY FIX
        producer.send(updatedEvent);

        // 🔥 USER_ROLE_CHANGED EVENT
        if (roleChanged) {
            UserEvent roleEvent = new UserEvent(
                    "USER_ROLE_CHANGED",
                    saved.getEmail(),
                    null,
                    saved.getRoles().replace("ROLE_", "")
            );
            roleEvent.setUserId(saved.getId()); // ✅ ONLY FIX
            producer.send(roleEvent);
        }

        return mapToResponse(saved);
    }

    // ----------------------------------------------------
    // DELETE USER
    // ----------------------------------------------------
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {

        User user = repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id " + id));

        repo.delete(user);

        UserEvent deleteEvent = new UserEvent(
                "USER_DELETED",
                user.getEmail(),
                null,
                null
        );
        deleteEvent.setUserId(user.getId()); // ✅ ONLY FIX
        producer.send(deleteEvent);
    }

    // ----------------------------------------------------
    // LIST USERS
    // ----------------------------------------------------
    public Page<UserResponse> listUsers(int page, int size, String sort, String dir) {

        Sort.Direction direction =
                "desc".equalsIgnoreCase(dir)
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Sort s = Sort.by(direction,
                (sort == null || sort.isEmpty()) ? "id" : sort);

        Pageable p = PageRequest.of(page, size, s);
        return repo.findAll(p).map(this::mapToResponse);
    }

    // ----------------------------------------------------
    // GET BY EMAIL
    // ----------------------------------------------------
    @Cacheable(value = "users", key = "#email")
    public UserResponse getByEmail(String email) {

        User user = repo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email " + email
                        ));

        return mapToResponse(user);
    }

    // ----------------------------------------------------
    // UPDATE BY EMAIL
    // ----------------------------------------------------
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateByEmail(String email, UpdateUserRequest req) {

        User u = repo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email " + email
                        ));

        boolean roleChanged = false;

        if (req.getDisplayName() != null) {
            u.setDisplayName(req.getDisplayName());
        }

        if (req.getRoles() != null && !req.getRoles().equals(u.getRoles())) {
            u.setRoles(req.getRoles());
            roleChanged = true;
        }

        User saved = repo.save(u);

        UserEvent updatedEvent = new UserEvent(
                "USER_UPDATED",
                saved.getEmail(),
                saved.getDisplayName(),
                null
        );
        updatedEvent.setUserId(saved.getId()); // ✅ ONLY FIX
        producer.send(updatedEvent);

        if (roleChanged) {
            UserEvent roleEvent = new UserEvent(
                    "USER_ROLE_CHANGED",
                    saved.getEmail(),
                    null,
                    saved.getRoles().replace("ROLE_", "")
            );
            roleEvent.setUserId(saved.getId()); // ✅ ONLY FIX
            producer.send(roleEvent);
        }

        return mapToResponse(saved);
    }
    public List<UserResponse> getUsersByRole(String role) {
        return repo.findUsersByRole(role)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    // ----------------------------------------------------
    // MAP ENTITY → RESPONSE
    // ----------------------------------------------------
    private UserResponse mapToResponse(User u) {
        UserResponse r = new UserResponse();
        r.setId(u.getId());
        r.setEmail(u.getEmail());
        r.setDisplayName(u.getDisplayName());
        r.setRoles(u.getRoles());
        r.setTenantId(u.getTenantId());
        r.setCreatedAt(u.getCreatedAt());
        return r;
    }
}
