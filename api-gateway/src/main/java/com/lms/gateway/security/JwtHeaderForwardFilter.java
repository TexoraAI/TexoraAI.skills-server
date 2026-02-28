//package com.lms.gateway.security;
//
//import io.jsonwebtoken.Claims;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.core.Ordered;
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//
//@Configuration
//public class JwtHeaderForwardFilter {
//
//    private final SecretKey key;
//
//    public JwtHeaderForwardFilter(@Value("${jwt.secret}") String secret) {
//        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//    }
//
//    @Bean
//    public GlobalFilter jwtHeaderForwardingFilter() {
//
//        return (exchange, chain) -> {
//
//            String authHeader =
//                    exchange.getRequest().getHeaders().getFirst("Authorization");
//
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//
//                String token = authHeader.substring(7);
//
//                Claims claims = Jwts.parserBuilder()
//                        .setSigningKey(key)
//                        .build()
//                        .parseClaimsJws(token)
//                        .getBody();
//
//                String role = claims.get("role", String.class);
//                String user = claims.getSubject();
//
//                ServerHttpRequest mutatedRequest =
//                        exchange.getRequest().mutate()
//                                .header("X-ROLE", role)
//                                .header("X-USER", user)
//                                .build();
//
//                exchange = exchange.mutate()
//                        .request(mutatedRequest)
//                        .build();
//            }
//
//            return chain.filter(exchange);
//        };
//        
//    }
//    @Bean
//    public Ordered jwtFilterOrder() {
//        return () -> -100; // execute BEFORE authorization filter
//    }
//    
//}
//
//
//
//

package com.lms.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtHeaderForwardFilter {

    private final SecretKey key;

    public JwtHeaderForwardFilter(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public GlobalFilter jwtHeaderForwardingFilter() {

        return (exchange, chain) -> {

            String authHeader =
                    exchange.getRequest().getHeaders().getFirst("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                try {

                    String token = authHeader.substring(7);

                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String role = claims.get("role", String.class);
                    String user = claims.getSubject();

                    System.out.println("JWT ROLE = " + role);
                    System.out.println("JWT USER = " + user);

                    ServerHttpRequest mutatedRequest =
                            exchange.getRequest().mutate()
                                    .header("X-ROLE", role)
                                    .header("X-USER", user)
                                    .build();

                    exchange = exchange.mutate()
                            .request(mutatedRequest)
                            .build();

                } catch (Exception e) {
                    System.out.println("JWT ERROR: " + e.getMessage());
                }
            }

            return chain.filter(exchange);
        };
    }

}

