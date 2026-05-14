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
                "https://ilm.ora.texora.ai",
                "https://*.vercel.app",
                "http://15.206.210.30"
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
