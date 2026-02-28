//package com.lms.auth.consumer;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lms.auth.event.UserEvent;
//import com.lms.auth.model.Role;
//import com.lms.auth.model.User;
//import com.lms.auth.repository.UserRepository;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//public class UserEventConsumer {
//
//    private final UserRepository userRepository;
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    public UserEventConsumer(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @KafkaListener(
//            topics = "auth-sync-events",
//            groupId = "auth-service-group"
//    )
//    public void consume(String message) {
//
//        try {
//            UserEvent event = mapper.readValue(message, UserEvent.class);
//
//            switch (event.getEventType()) {
//
//                case "USER_ROLE_CHANGED" -> {
//                    userRepository.findByEmail(event.getEmail())
//                            .ifPresent(user -> {
//                                user.setRole(Role.valueOf(event.getRole()));
//                                userRepository.save(user);
//                            });
//                }
//
//                case "USER_UPDATED" -> {
//                    userRepository.findByEmail(event.getEmail())
//                            .ifPresent(user -> {
//                                user.setName(event.getDisplayName());
//                                userRepository.save(user);
//                            });
//                }
//
//                case "USER_DELETED" -> {
//                    userRepository.findByEmail(event.getEmail())
//                            .ifPresent(userRepository::delete);
//                }
//            }
//
//            System.out.println("✅ USER EVENT APPLIED IN AUTH → " + event.getEventType());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}





package com.lms.auth.consumer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.auth.event.UserEvent;
import com.lms.auth.model.Role;
import com.lms.auth.repository.EmailVerificationTokenRepository;
import com.lms.auth.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserEventConsumer {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserEventConsumer(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository
    ) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
    }

    @KafkaListener(
            topics = "auth-sync-events",
            groupId = "auth-service-group"
    )
    @org.springframework.transaction.annotation.Transactional  // 🔥 THIS LINE
    public void consume(String message) {

        try {
            UserEvent event = mapper.readValue(message, UserEvent.class);

            switch (event.getEventType()) {

                case "USER_DELETED" -> {
                    userRepository.findByEmail(event.getEmail())
                            .ifPresent(user -> {

                                // ✅ FIRST delete child records
                                emailVerificationTokenRepository
                                        .deleteByUserId(user.getId());

                                // ✅ THEN delete parent
                                userRepository.delete(user);
                            });
                }

                case "USER_UPDATED" -> {
                    userRepository.findByEmail(event.getEmail())
                            .ifPresent(user -> {
                                user.setName(event.getDisplayName());
                                userRepository.save(user);
                            });
                }

                case "USER_ROLE_CHANGED" -> {
                    userRepository.findByEmail(event.getEmail())
                            .ifPresent(user -> {
                                user.setRole(Role.valueOf(event.getRole()));
                                userRepository.save(user);
                            });
                }
            }

            System.out.println("✅ USER EVENT APPLIED IN AUTH → " + event.getEventType());

        } catch (Exception e) {
            System.err.println("❌ AUTH EVENT FAILED");
            e.printStackTrace();
        }
    }
}
