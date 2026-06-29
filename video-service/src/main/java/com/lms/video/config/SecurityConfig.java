//
//
//
//package com.lms.video.config;
//
//import com.lms.video.security.JwtFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.http.HttpMethod;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    private final JwtFilter jwtFilter;
//
//    public SecurityConfig(JwtFilter jwtFilter) {
//        this.jwtFilter = jwtFilter;
//    }
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//        http.csrf(csrf -> csrf.disable());
//
//        http.sessionManagement(session ->
//                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//        );
//
//        http.authorizeHttpRequests(auth ->
//                auth
//                    .requestMatchers("/error").permitAll()
//
//                    // ✅ PUBLIC VIDEO STREAMING
//                    .requestMatchers(HttpMethod.GET, "/api/video/play/**").permitAll()
//
//                    .requestMatchers("/api/course-videos/stream/**").permitAll()
//                    
//                    // ✅ PUBLIC LIST VIDEOS
//                    .requestMatchers(HttpMethod.GET, "/api/video").permitAll()
//
//                    // ✅ PUBLIC GET VIDEO BY ID
//                    .requestMatchers(HttpMethod.GET, "/api/video/**").permitAll()
//
//                    
//                    .requestMatchers(HttpMethod.GET, "/api/course-videos/**").permitAll()
//                    
//                    .requestMatchers(HttpMethod.POST, "/api/upload-course/**").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/upload-course/all").permitAll()
//                    .requestMatchers(HttpMethod.GET, "/api/upload-course/stream/**").permitAll()
//                    // 🔐 upload/delete should still require login
//                    .requestMatchers("/api/video-feature-flags/**").permitAll()
//                    .anyRequest().authenticated()
//        );
//
//        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        http.httpBasic(httpBasic -> httpBasic.disable());
//        http.formLogin(form -> form.disable());
//
//        return http.build();
//    }
//}
//



package com.lms.video.config;

import com.lms.video.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;

/**
 * Video-service internal Spring Security config.
 *
 * RULE SUMMARY
 * ──────────────────────────────────────────────────────────────────
 * PUBLIC (no token needed):
 *   GET  /api/video/play/**           – play uploaded video blobs
 *   GET  /api/video                   – list all videos
 *   GET  /api/video/**                – get video by id
 *   GET  /api/course-videos/**        – course module video access
 *   GET  /api/course-videos/stream/** – stream course video
 *   GET  /api/v1/watch-now/all        – list all WatchNow entries
 *   GET  /api/v1/watch-now/{id}       – single WatchNow entry
 *   GET  /api/v1/watch-now/stream/**  – stream WatchNow video/thumbnail
 *   GET  /api/video-feature-flags/**  – feature flag reads
 *
 * AUTHENTICATED (SUPER_ADMIN enforced at Gateway layer):
 *   POST   /api/v1/watch-now/upload   – create WatchNow
 *   PUT    /api/v1/watch-now/**       – update WatchNow
 *   DELETE /api/v1/watch-now/**       – delete WatchNow
 *   …everything else…
 *
 * NOTE: Role-level access (SUPER_ADMIN vs ADMIN vs TRAINER etc.) is
 * enforced by GatewaySecurityConfig in the API-Gateway service.
 * This service only needs to verify the JWT is present and valid for
 * non-public endpoints.
 */
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

        http.authorizeHttpRequests(auth -> auth

            // ── error ──────────────────────────────────────────────────────
            .requestMatchers("/error").permitAll()

            // ══ OLD VIDEO LIBRARY (keep existing behaviour) ════════════════
            .requestMatchers(HttpMethod.GET, "/api/video/play/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/video").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/video/**").permitAll()

            // ══ COURSE MODULE VIDEOS (keep existing behaviour) ══════════════
            .requestMatchers(HttpMethod.GET, "/api/course-videos/stream/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/course-videos/**").permitAll()

            // ══ WATCH NOW – public GET endpoints ════════════════════════════
            .requestMatchers(HttpMethod.GET, "/api/v1/watch-now/all").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/watch-now/stream/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/watch-now/**").permitAll()

            // ══ VIDEO FEATURE FLAGS – public reads ══════════════════════════
            .requestMatchers(HttpMethod.GET, "/api/video-feature-flags/**").permitAll()

            // ── Everything else requires a valid JWT ───────────────────────
            .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(httpBasic -> httpBasic.disable());
        http.formLogin(form -> form.disable());

        return http.build();
    }
}