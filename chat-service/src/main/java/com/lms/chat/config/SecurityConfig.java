

package com.lms.chat.config;

import com.lms.chat.security.JwtAuthFilter;
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

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/batch/student").hasRole("STUDENT")
                .requestMatchers("/api/batch/trainer/**").hasRole("TRAINER")
                
                .requestMatchers("/api/feedback/submit").hasRole("STUDENT")
                .requestMatchers("/api/feedback/student/**").hasRole("STUDENT")
                .requestMatchers("/api/feedback/trainer/**").hasRole("TRAINER")
                .requestMatchers("/api/feedback/admin/**").hasRole("ADMIN")

             // ================= NOTEBOOKS (✅ ADD THIS) =================
                .requestMatchers("/api/notebooks/**")
                    .hasAnyRole("STUDENT", "TRAINER", "ADMIN")

                
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthFilter,
                org.springframework.security.web.access.intercept.AuthorizationFilter.class);

        return http.build();
    }
}
