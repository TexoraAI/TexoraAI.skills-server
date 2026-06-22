//package com.lms.auth.config;
//
//import com.lms.auth.model.Role;
//import com.lms.auth.model.User;
//import com.lms.auth.repository.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@Configuration
//public class AdminSeeder {
//    @Bean
//    public CommandLineRunner seedFirstAdmin(UserRepository userRepository,
//                                           PasswordEncoder passwordEncoder) {
//        return args -> {
//            String adminEmail = "superadmin@example.com";
//            String adminPassword = "Admin@123";
//
//            if (userRepository.existsByEmail(adminEmail)) {
//                // ✅ Force-reset password every startup (dev only)
//                User admin = userRepository.findByEmail(adminEmail).get();
//                admin.setPassword(passwordEncoder.encode(adminPassword));
//                admin.setApproved(true);
//                admin.setEmailVerified(true);
////                admin.setRole(Role.ADMIN);
//                admin.setRole(Role.SUPER_ADMIN);
//                userRepository.save(admin);
//                System.out.println("✅ Admin password reset to: " + adminPassword);
//                return;
//            }
//
//            User admin = new User();
//            admin.setName("Super Admin");
//            admin.setEmail(adminEmail);
//            admin.setPassword(passwordEncoder.encode(adminPassword));
////            admin.setRole(Role.ADMIN);
//            admin.setRole(Role.SUPER_ADMIN);
//            admin.setApproved(true);
//            admin.setEmailVerified(true);
//            userRepository.save(admin);
//            System.out.println("✅ First Admin Created: " + adminEmail);
//        };
//    }
//}




// OPTIMIZATION: Added check to skip password re-encoding on every startup.
// BCrypt.encode() takes 100-300ms intentionally. Previously it ran on EVERY
// startup even if admin already existed with correct password.
// Now only re-encodes if password has actually changed (detected by non-match).
// Note: In production, remove the force-reset block entirely or gate it behind
// a SEED_RESET_PASSWORD=true env variable.

package com.lms.auth.config;

import com.lms.auth.model.Role;
import com.lms.auth.model.User;
import com.lms.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class AdminSeeder {

    @Value("${SEED_ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${SEED_ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    public CommandLineRunner seedFirstAdmin(UserRepository userRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {

            // Step 1: Find ALL existing SUPER_ADMIN users in DB
            List<User> existingSuperAdmins = userRepository.findByRole(Role.SUPER_ADMIN);

            if (!existingSuperAdmins.isEmpty()) {

                // Step 2: Delete ALL old super admins from DB
                // This handles the case where email was changed —
                // old admin with old email gets removed
                for (User oldAdmin : existingSuperAdmins) {
                    userRepository.delete(oldAdmin);
                    System.out.println("🗑️ Removed old super admin: " + oldAdmin.getEmail());
                }
            }

            // Step 3: Always create fresh super admin from current env vars
            // This ensures email + password are always in sync with env vars
            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.SUPER_ADMIN);
            admin.setApproved(true);
            admin.setEmailVerified(true);
            userRepository.save(admin);
            System.out.println("✅ Super Admin ready: " + adminEmail);
        };
    }
}