//package com.lms.user.kafka;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lms.user.event.AuthEvent;
//import com.lms.user.model.User;
//import com.lms.user.repo.UserRepository;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//public class AuthEventConsumer {
//
//    private final UserRepository userRepository;
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public AuthEventConsumer(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @KafkaListener(
//            topics = "user-events",          // ✅ FIXED
//            groupId = "user-service-group"
//    )
//    public void consume(String message) {
//
//        try {
//            AuthEvent event = objectMapper.readValue(message, AuthEvent.class);
//
//            switch (event.getEventType()) {
//
//                case "USER_CREATED" -> handleCreate(event);
//
//                case "USER_DELETED" -> handleDelete(event);
//
//                case "USER_ROLE_CHANGED" -> handleRoleChange(event);
//
//                default -> {
//                    // ignore
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void handleCreate(AuthEvent event) {
//
//        if (userRepository.findByEmail(event.getEmail()).isPresent()) {
//            return;
//        }
//
//        User user = new User();
//        user.setEmail(event.getEmail());
//        user.setDisplayName(event.getDisplayName());
//        user.setRoles("ROLE_" + event.getRole());
//
//        userRepository.save(user);
//
//        System.out.println("✅ User created in USER-SERVICE: " + event.getEmail());
//    }
//
//    private void handleDelete(AuthEvent event) {
//
//        userRepository.findByEmail(event.getEmail())
//                .ifPresent(userRepository::delete);
//
//        System.out.println("❌ User deleted in USER-SERVICE: " + event.getEmail());
//    }
//
//    private void handleRoleChange(AuthEvent event) {
//
//        userRepository.findByEmail(event.getEmail())
//                .ifPresent(user -> {
//                    user.setRoles("ROLE_" + event.getRole());
//                    userRepository.save(user);
//                });
//
//        System.out.println("🔁 Role updated in USER-SERVICE: " + event.getEmail());
//    }
//}



package com.lms.user.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.user.event.AuthEvent;
import com.lms.user.model.User;
import com.lms.user.repo.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AuthEventConsumer {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthEventConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ MUST listen to AUTH-SERVICE topic
    @KafkaListener(
            topics = "auth-events",
            groupId = "user-service-group"
    )
    public void consume(String message) {

        try {
            AuthEvent event = objectMapper.readValue(message, AuthEvent.class);

            switch (event.getEventType()) {

                case "USER_CREATED" -> handleCreate(event);

                case "USER_DELETED" -> handleDelete(event);

                case "USER_ROLE_CHANGED" -> handleRoleChange(event);

                default -> {
                    // safely ignore
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------- HANDLERS ----------------

    private void handleCreate(AuthEvent event) {

        if (userRepository.findByEmail(event.getEmail()).isPresent()) {
            return;
        }

        User user = new User();
        user.setEmail(event.getEmail());
        user.setDisplayName(event.getDisplayName());
        user.setRoles("ROLE_" + event.getRole());

        userRepository.save(user);

        System.out.println("✅ USER-SERVICE: User created → " + event.getEmail());
    }

    private void handleDelete(AuthEvent event) {

        userRepository.findByEmail(event.getEmail())
                .ifPresent(userRepository::delete);

        System.out.println("❌ USER-SERVICE: User deleted → " + event.getEmail());
    }

    private void handleRoleChange(AuthEvent event) {

        userRepository.findByEmail(event.getEmail())
                .ifPresent(user -> {
                    user.setRoles("ROLE_" + event.getRole());
                    userRepository.save(user);
                });

        System.out.println("🔁 USER-SERVICE: Role updated → " + event.getEmail());
    }
}
