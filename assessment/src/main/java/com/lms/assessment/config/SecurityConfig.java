//
//package com.lms.assessment.config;
//
//import com.lms.assessment.security.JwtFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//@EnableMethodSecurity
//public class SecurityConfig {
//
//    private final JwtFilter jwtFilter;
//
//    public SecurityConfig(JwtFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//            .csrf(csrf -> csrf.disable())
//
//            .sessionManagement(session ->
//                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            )
//
//            .authorizeHttpRequests(auth -> auth
//
//                /* ================= QUIZ ================= */
//                .requestMatchers("/api/quizzes/**").authenticated()
//                .requestMatchers("/api/questions/**").authenticated()
//                .requestMatchers("/api/options/**").authenticated()
//                .requestMatchers("/api/attempts/**").authenticated()
////                // ✅ FIX: Bulk Upload only trainer can access
////                .requestMatchers(HttpMethod.POST, "/api/quizzes/upload-bulk")
////                .hasRole("TRAINER")
//
//
//                /* ================= ASSIGNMENT ================= */
//                .requestMatchers("/api/assignments/**").authenticated()
//                .requestMatchers("/api/assignment-files/**").authenticated()
//                .requestMatchers("/api/submissions/**").authenticated()
//
//                /* Everything else */
//                .anyRequest().authenticated()
//            )
//
//            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}


package com.lms.assessment.config;

import com.lms.assessment.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ❌ CSRF disable (API use case)
            .csrf(csrf -> csrf.disable())

            // ❌ No session (JWT based)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                /* ================= PUBLIC (if needed) ================= */
                // .requestMatchers("/api/auth/**").permitAll()

                /* ================= QUIZ ================= */

                // ✅ FIX: Bulk Upload only trainer can access
                .requestMatchers(HttpMethod.POST, "/api/quizzes/upload-bulk")
                .hasRole("TRAINER")

                // बाकी quiz APIs → login required
                .requestMatchers("/api/quizzes/**").authenticated()

                /* ================= QUESTION ================= */
                .requestMatchers("/api/questions/**").authenticated()

                /* ================= OPTION ================= */
                .requestMatchers("/api/options/**").authenticated()

                /* ================= ATTEMPT ================= */
                .requestMatchers("/api/attempts/**").authenticated()

                /* ================= ASSIGNMENT ================= */
                .requestMatchers("/api/assignments/**").authenticated()
                .requestMatchers("/api/assignment-files/**").authenticated()
                .requestMatchers("/api/submissions/**").authenticated()

                /* ================= DEFAULT ================= */
                .anyRequest().authenticated()
            )

            // ✅ JWT filter add
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
