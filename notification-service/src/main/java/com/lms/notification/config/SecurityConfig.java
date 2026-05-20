package com.lms.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/ws/**",
                		  "/ws/info",         // ✅ SockJS info endpoint
                          "/ws/info/**",  
                                 "/api/notification/health").permitAll()
             // ✅ Homepage newsletter public
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/notification/newsletter/subscribe"
                ).permitAll()

                // ✅ Newsletter unsubscribe public
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/notification/newsletter/unsubscribe"
                ).permitAll()

                // ✅ Contact-us public
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/notification/contact"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter,
                    UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}