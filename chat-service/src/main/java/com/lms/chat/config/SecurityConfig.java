//package com.lms.chat.config;
//import com.lms.chat.security.JwtAuthFilter;
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
//            // no csrf (microservice)
//            .csrf(csrf -> csrf.disable())
//
//            // VERY IMPORTANT (otherwise anonymousUser happens)
//            .sessionManagement(session ->
//                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            )
//
//            .authorizeHttpRequests(auth -> auth
//
//                // gateway preflight
//                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//
//                // chat endpoints → any logged user
//                .requestMatchers("/api/chat/**")
//                    .hasAnyRole("STUDENT", "TRAINER", "ADMIN")
//
//                // everything else blocked
//                .anyRequest().authenticated()
//            )
//
//            // JWT FILTER MUST RUN BEFORE SPRING AUTH
//            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}

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
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthFilter,
                org.springframework.security.web.access.intercept.AuthorizationFilter.class);

        return http.build();
    }
}
