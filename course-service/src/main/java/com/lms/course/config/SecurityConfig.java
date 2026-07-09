

package com.lms.course.config;

import com.lms.course.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
            .cors(cors -> {})
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .authorizeHttpRequests(auth -> auth

                // 🔓 PUBLIC PREVIEW APIs
                .requestMatchers(
                        "/api/courses/{id}",
                        "/api/courses/*",
                        "/api/content/course/**",
                        "/api/content/student/course/**",
                        "/api/featured-courses/**",
                       " /api/course-feature-flags/**",
                       "/api/course/v1/featurecourse/**" ,
                       "/api/course/v1/wishlist/**", 
                       "/api/v1/mentor-feedback/public/**",
                       "/api/v1/companies/public/**",
                       "/api/v1/cmslandinghubs/public/**",
                       "/api/v1/cmslandinghubs/media/*/raw"
                       
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/banners/**").permitAll()
                .requestMatchers(HttpMethod.PATCH, "/api/banners/*/view", "/api/banners/*/click").permitAll()
                // 🔐 Everything else requires JWT
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

