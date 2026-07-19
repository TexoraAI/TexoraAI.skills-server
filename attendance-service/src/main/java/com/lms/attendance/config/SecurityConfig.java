
package com.lms.attendance.config;

import com.lms.attendance.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        System.out.println("🔥 SecurityConfig LOADED 🔥");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm ->
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                    // NEW — more specific matcher MUST come before the general
                    // /api/trainer/** rule below, since Spring Security uses
                    // first-match-wins ordering. This mirrors the gateway's
                    // ADMIN/TENANT_ADMIN/SUPER_ADMIN allowance for this same
                    // sub-path, without loosening any other /api/trainer/** route.
                    .requestMatchers("/api/trainer/attendance/**")
                        .hasAnyRole("TRAINER", "ADMIN", "TENANT_ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/attendance-feature-flags/**")
                        .hasAnyRole("ADMIN", "TENANT_ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/trainer/**").hasRole("TRAINER")
                    .requestMatchers("/api/student/**").hasRole("STUDENT")
                    .anyRequest().authenticated()
            )
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}