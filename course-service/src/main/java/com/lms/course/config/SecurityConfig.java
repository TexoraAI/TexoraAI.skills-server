////package com.lms.course.config;
////
////import com.lms.course.security.JwtFilter;
////import org.springframework.context.annotation.Bean;
////import org.springframework.context.annotation.Configuration;
////import org.springframework.security.config.annotation.web.builders.HttpSecurity;
////import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
////import org.springframework.security.web.SecurityFilterChain;
////import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
////
////@Configuration
////@EnableWebSecurity
////public class SecurityConfig {
////
////    private final JwtFilter jwtFilter;
////
////    public SecurityConfig(JwtFilter jwtFilter) {
////        this.jwtFilter = jwtFilter;
////    }
////
////    @Bean
////    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
////
////        http
////            .csrf(csrf -> csrf.disable())
////            .cors(cors -> {})
////            .formLogin(form -> form.disable())   // ❌ disable default login
////            .httpBasic(basic -> basic.disable()) // ❌ disable basic auth
////            .authorizeHttpRequests(auth -> auth
////                .requestMatchers("/api/**").authenticated()
////                .anyRequest().permitAll()
////            )
////            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
////
////        return http.build();
////    }
////}
//
//
//package com.lms.course.config;
//
//import com.lms.course.security.JwtFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http
//            .csrf(csrf -> csrf.disable())
//            .cors(cors -> {})
//            .formLogin(form -> form.disable())
//            .httpBasic(basic -> basic.disable())
//
//            .authorizeHttpRequests(auth -> auth
//
//                // 🔓 PUBLIC COURSE PREVIEW (no JWT)
//                .requestMatchers("/api/courses/*").permitAll()
//
//                // 🔓 PUBLIC COURSE CONTENT (preview modules)
//                .requestMatchers("/api/content/course/*").permitAll()
//
//                // 🔐 ALL OTHER APIs REQUIRE JWT
//                .requestMatchers("/api/**").authenticated()
//
//                .anyRequest().permitAll()
//            )
//
//            // JWT filter must still run for secured endpoints
//            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//}
//
package com.lms.course.config;

import com.lms.course.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                        "/api/featured-courses/**"
                ).permitAll()

                // 🔐 Everything else requires JWT
                .requestMatchers("/api/**").authenticated()

                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

