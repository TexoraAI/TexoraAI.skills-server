//package com.lms.gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//
////@Configuration
////public class CorsConfig {
////
////    @Bean
////    public CorsWebFilter corsWebFilter() {
////        CorsConfiguration config = new CorsConfiguration();
////
////        // ✅ allow frontend
////        config.addAllowedOrigin("http://localhost:5173");
////
////        // ✅ allow headers
////        config.addAllowedHeader("*");
////
////        // ✅ allow methods
////        config.addAllowedMethod("*");
////
////        // ✅ allow JWT
////        config.setAllowCredentials(true);
////
////        UrlBasedCorsConfigurationSource source =
////                new UrlBasedCorsConfigurationSource();
////
////        source.registerCorsConfiguration("/**", config);
////
////        return new CorsWebFilter(source);
////    }
////}
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//
//        CorsConfiguration config = new CorsConfiguration();
//
//        // ✅ allow frontend (pattern-based)
//        config.addAllowedOriginPattern("http://localhost:5173");
//
//        // ✅ allow all headers
//        config.addAllowedHeader("*");
//
//        // ✅ allow all HTTP methods
//        config.addAllowedMethod("*");
//
//        // ✅ allow cookies / auth headers
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source =
//                new UrlBasedCorsConfigurationSource();
//
//        source.registerCorsConfiguration("/**", config);
//
//        return new CorsWebFilter(source);
//    }
//}



//package com.lms.gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//
//        CorsConfiguration config = new CorsConfiguration();
//
//        // ✅ allow local frontend
//        config.addAllowedOriginPattern("http://localhost:5173");
//
//        // ✅ allow Vercel frontend
//        config.addAllowedOriginPattern("https://texora-ai-skills-client-seven.vercel.app");
//
//        // ✅ allow all vercel preview deployments (optional but recommended)
//        config.addAllowedOriginPattern("https://*.vercel.app");
//
//        config.addAllowedHeader("*");
//        config.addAllowedMethod("*");
//        config.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source =
//                new UrlBasedCorsConfigurationSource();
//
//        source.registerCorsConfiguration("/**", config);
//
//        return new CorsWebFilter(source);
//    }
//}




package com.lms.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {

        CorsConfiguration config = new CorsConfiguration();

        // ✅ Allow localhost + your Vercel frontend
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://texora-ai-skills-client-seven.vercel.app",
                "https://*.vercel.app"
        ));

        // ✅ Allow all headers
        config.addAllowedHeader("*");

        // ✅ Allow all methods (GET,POST,PUT,DELETE,OPTIONS)
        config.addAllowedMethod("*");

        // ✅ Allow credentials (Authorization header, cookies)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);
//ffhbhfbfhv
        return new CorsWebFilter(source);
    }
}
