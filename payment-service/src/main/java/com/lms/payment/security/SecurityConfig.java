package com.lms.payment.security;

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

    // Only these three roles are allowed to touch payment/wallet endpoints.
    // super_admin is deliberately NOT included — that role never initiates,
    // pays for, or holds a wallet, so it gets no access here at all.
    private static final String[] PAYMENT_ROLES = { "STUDENT", "TRAINER", "TENANT_ADMIN" };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                    // health checks
                    .requestMatchers("/actuator/**").permitAll()

                    // Razorpay calls this directly with no JWT — it's authenticated
                    // via the X-Razorpay-Signature header instead, verified inside
                    // PaymentService.handleWebhook().
                    .requestMatchers("/api/payments/webhook/razorpay").permitAll()

                    // Everything else in payments/wallet requires one of the three
                    // payment-eligible roles, scoped to the caller's own organizationId
                    // (enforced in the service layer using AuthenticatedUser).
                    .requestMatchers("/api/payments/**").hasAnyRole(PAYMENT_ROLES)
                    .requestMatchers("/api/wallet/**").hasAnyRole(PAYMENT_ROLES)

                    // everything else requires authentication
                    .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
