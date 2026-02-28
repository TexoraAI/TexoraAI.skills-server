////
//
//package com.lms.batch.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
//public class SecurityConfig {
//
//    private final JwtAuthFilter jwtAuthFilter;
//
//    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
//        this.jwtAuthFilter = jwtAuthFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//            .csrf(csrf -> csrf.disable())
//
//            // ❌ DO NOT enable cors here in microservice
//            // .cors()  ← MUST NOT EXIST
//
//            .sessionManagement(session ->
//                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            )
//
//            .authorizeHttpRequests(auth -> auth
//
//                    // allow preflight (gateway sends OPTIONS)
//                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                    // PUBLIC
//                    .requestMatchers("/api/auth/**").permitAll()
//
//                    // ADMIN
//                    .requestMatchers("/api/branch/**").hasRole("ADMIN")
//                    .requestMatchers("/api/batch/admin/**").hasRole("ADMIN")
//
//                    // TRAINER
//                    .requestMatchers("/api/batch/trainer/**").hasRole("TRAINER")
//
//                    // STUDENT
//                    .requestMatchers("/api/batch/student/**").hasRole("STUDENT")
//                    
//                    // ADMIN FULL ACCESS
//                    .requestMatchers("/api/batch/admin/**").hasRole("ADMIN")
//
//                    .requestMatchers(HttpMethod.PUT, "/api/batch/admin/**").hasRole("ADMIN")
//                    .requestMatchers(HttpMethod.DELETE, "/api/batch/admin/**").hasRole("ADMIN")
//                    
//                    // IMPORTANT 🔥 allow DELETE
//                    .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
//
//                    // allow PUT (assign trainer)
//                    .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
//
//
//                    .anyRequest().authenticated()
//            )
//
//            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}


package com.lms.batch.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            // ⚠️ no cors here (gateway handles it)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // allow gateway preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // PUBLIC
                .requestMatchers("/api/auth/**").permitAll()

                // ADMIN APIs
                .requestMatchers("/api/branch/**").hasRole("ADMIN")
                .requestMatchers("/api/batch/admin/**").hasRole("ADMIN")

                // TRAINER APIs
                .requestMatchers("/api/batch/trainer/**").hasRole("TRAINER")

                // STUDENT APIs
                .requestMatchers("/api/batch/student/**").hasRole("STUDENT")

                // everything else must be authenticated
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
