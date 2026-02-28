//
//
//
//
//package com.lms.auth.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//            .csrf(csrf -> csrf.disable())
//
//            // ✅ Keep CORS disabled here (Gateway handles it)
//            // .cors(cors -> cors.disable())
//
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                        "/api/auth/register",
//                        "/api/auth/login",
//                        "/api/auth/google",
//                        "/api/auth/forgot-password",
//                        "/api/auth/reset-password",
//                        "/api/auth/verify-email",
//                        "/api/auth/resend-verification"
//                        
//
//                ).permitAll()
//
//                // allow swagger also if you use
//                .requestMatchers(
//                        "/v3/api-docs/**",
//                        "/swagger-ui/**",
//                        "/swagger-ui.html"
//                ).permitAll()
//
//                .anyRequest().permitAll()
//            );
//
//        return http.build();
//    }
//
//    // ✅ Password encoder (REQUIRED)
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}






package com.lms.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // ✅ Keep CORS disabled here (Gateway handles it)
                // .cors(cors -> cors.disable())

                .authorizeHttpRequests(auth -> auth
                        // ✅ Auth endpoints (PUBLIC)
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/google",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification"
                        ).permitAll()

                        // ✅ Approval endpoints (ADMIN will access)
                        // for now permitAll (later we secure using JWT + ROLE_ADMIN)
                        .requestMatchers(
                                "/api/admin/approval/**"
                        ).permitAll()

                        // ✅ Status check endpoint (for ApprovalPending page)
                        .requestMatchers(
                                "/api/status/**"
                        ).permitAll()

                        // ✅ allow swagger also if you use
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // ✅ allow everything else for now
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    // ✅ Password encoder (REQUIRED)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
