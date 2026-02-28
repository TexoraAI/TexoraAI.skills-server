package com.lms.auth.config;

import com.lms.auth.model.Role;
import com.lms.auth.model.User;
import com.lms.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    public CommandLineRunner seedFirstAdmin(UserRepository userRepository,
                                           PasswordEncoder passwordEncoder) {

        return args -> {

            String adminEmail = "admin@example.com";
            String adminPassword = "Admin@123"; // change if you want

            // ✅ If admin already exists, skip
            if (userRepository.existsByEmail(adminEmail)) {
                System.out.println("✅ Admin already exists. Seeder skipped.");
                return;
            }

            // ✅ Create first admin
            User admin = new User();
            admin.setName("Super Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);

            // ✅ IMPORTANT: make him active
            admin.setApproved(true);
            admin.setEmailVerified(true);

            userRepository.save(admin);

            System.out.println("✅ First Admin Created Successfully!");
            System.out.println("📧 Email: " + adminEmail);
            System.out.println("🔑 Password: " + adminPassword);
        };
    }
}
