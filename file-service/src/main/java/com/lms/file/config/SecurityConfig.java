
package com.lms.file.config;

import com.lms.file.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpMethod;

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

            
            
            // download allowed without login
            .requestMatchers(HttpMethod.GET, "/api/file/download/**").permitAll()

         // 🔥 COURSE MODULE FILE STREAM (requires login)
            .requestMatchers("/api/course-files/stream/**").authenticated()

            // 🔥 COURSE MODULE FILE UPLOAD (requires login)
            .requestMatchers("/api/course-files/upload").authenticated()

            // 🔥 COURSE MODULE FILE DELETE
            .requestMatchers("/api/course-files/**").authenticated()
            
            
            
            // everything else requires login
            .anyRequest().authenticated()
);


        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        http.httpBasic(httpBasic -> httpBasic.disable());
        http.formLogin(form -> form.disable());

        return http.build();
    }
}

