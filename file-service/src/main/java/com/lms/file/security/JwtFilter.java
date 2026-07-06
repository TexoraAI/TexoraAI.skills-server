
package com.lms.file.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                Collections.emptyList()
                        );
                auth.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

                // NEW — stash organizationId on the request so downstream service
                // code can read it without changing getName()/principal behavior.
                // null for standalone users — intentional.
                String organizationId = jwtUtil.extractOrganizationId(token);
             // Inside doFilterInternal, right after the existing organizationId line:
                String role = jwtUtil.extractRole(token);
                request.setAttribute("role", role);
                request.setAttribute("organizationId", organizationId);
            }
        }
        filterChain.doFilter(request, response);
    }
}