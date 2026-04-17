package com.lms.progress.config;

import com.lms.progress.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());

        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        http.authorizeHttpRequests(auth ->
                auth
                    .requestMatchers("/error").permitAll()

                    // ✅ Student endpoints — gateway already validated role
                    // so just let authenticated requests through here
                    .requestMatchers("/api/progress/mark-complete").authenticated()
                    .requestMatchers("/api/progress/user").authenticated()
                    .requestMatchers("/api/progress/reports/**").authenticated()
                    .requestMatchers("/api/video-progress/**").authenticated()
                    .requestMatchers("/api/file-progress/**").authenticated()
                    .requestMatchers("/api/assignment-progress/**").authenticated()
                    .requestMatchers("/api/quiz-progress/**").authenticated()
                 // ✅ REPORT APIs
                   

                    // Everything else — authenticated
                    .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(httpBasic -> httpBasic.disable());
        http.formLogin(form -> form.disable());

        return http.build();
    }
}