


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
//                .requestMatchers("/api/branch/**").hasRole("ADMIN")
//                .requestMatchers("/api/branch/**").hasAnyRole("ADMIN", "TENANT_ADMIN")
//                .requestMatchers("/api/batch/admin/**").hasAnyRole("ADMIN","TENANT_ADMIN")
//                
//                .requestMatchers("/api/departments/**").hasRole("TENANT_ADMIN")
                .requestMatchers("/api/branch/**").hasAnyRole("ADMIN", "TENANT_ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/batch/admin/**").hasAnyRole("ADMIN","TENANT_ADMIN","SUPER_ADMIN")
                .requestMatchers("/api/departments/**").hasAnyRole("TENANT_ADMIN","SUPER_ADMIN")
                // TRAINER APIs
                .requestMatchers("/api/batch/trainer/**").hasRole("TRAINER")

                // STUDENT APIs
                .requestMatchers("/api/batch/student/**").hasRole("STUDENT")

                .requestMatchers("/api/feature-flags/**").hasAnyRole("ADMIN", "TENANT_ADMIN", "SUPER_ADMIN")
//                
                // everything else must be authenticated
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
