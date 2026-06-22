//package com.lms.live_session.security;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//@Configuration
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
//            .sessionManagement(session ->
//                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            )
//            .authorizeHttpRequests(auth -> auth
//
//                    // allow health checks
//                    .requestMatchers("/actuator/**").permitAll()
//
//                    .requestMatchers("/live-chat/**").permitAll()
//                    .requestMatchers("/live-chat-sockjs/**").permitAll()
//                    .requestMatchers("/live-chat/**").permitAll()
//                    .requestMatchers("/api/live-sessions/public/**").permitAll()
//                    .requestMatchers("/api/live-sessions/public/upcoming").permitAll()
//                    // ✅ single session details — needed for booking form
//                    .requestMatchers("/api/live-sessions/{id}").permitAll()
//                    
//                    .requestMatchers("/api/v1/ai-companion/**").permitAll()      // ← ADD
//                    .requestMatchers("/api/v1/live-sessions/*/whiteboard/**").permitAll()
////                    .requestMatchers("/api/v1/live-sessions/**/whiteboard/**").permitAll() //
//                    // everything else requires authentication
//                    
//                    .requestMatchers("/api/live-sessions/v1/**").permitAll() 
//                    .requestMatchers("/api/live-sessions/v1/booking/public/**").permitAll()        
//            
//                    .anyRequest().authenticated()
//            );
//
//        http.addFilterBefore(jwtFilter,
//                UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}

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
                    .requestMatchers("/api/live-sessions/public/**").permitAll()
                    .requestMatchers("/api/live-sessions/public/upcoming").permitAll()
                    // single session details — needed for booking form
                    .requestMatchers("/api/live-sessions/{id}").permitAll()

                    .requestMatchers("/api/v1/ai-companion/**").permitAll()
                    .requestMatchers("/api/v1/live-sessions/*/whiteboard/**").permitAll()

                    // FIX 5: REMOVED the overly-broad wildcard below.
                    // The line below was removed:
                    //   .requestMatchers("/api/live-sessions/v1/**").permitAll()
                    //
                    // Only the specific public booking sub-path remains open:
                    .requestMatchers("/api/live-sessions/v1/booking/**").permitAll()
                    .requestMatchers("/live-sessions/v1/booking/**").permitAll()
                    // everything else requires authentication
                    .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}