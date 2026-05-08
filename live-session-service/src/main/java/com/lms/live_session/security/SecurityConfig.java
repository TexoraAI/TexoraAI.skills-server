package com.lms.live_session.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth

                    // allow health checks
                    .requestMatchers("/actuator/**").permitAll()

                    .requestMatchers("/live-chat/**").permitAll()
                    .requestMatchers("/live-chat-sockjs/**").permitAll()
                    .requestMatchers("/live-chat/**").permitAll()
                    .requestMatchers("/api/live-sessions/public/**").permitAll()
                    .requestMatchers("/api/live-sessions/public/upcoming").permitAll()
                    // ✅ single session details — needed for booking form
                    .requestMatchers("/api/live-sessions/{id}").permitAll()
                    // everything else requires authentication
                    .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}