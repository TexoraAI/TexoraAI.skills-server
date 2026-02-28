package com.lms.batch.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityDebugHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("\n================ SECURITY BLOCKED ================");
        System.out.println("PATH      : " + request.getRequestURI());
        System.out.println("METHOD    : " + request.getMethod());

        if (auth != null) {
            System.out.println("USER      : " + auth.getName());
            System.out.println("ROLES     : " + auth.getAuthorities());
        } else {
            System.out.println("USER      : NOT AUTHENTICATED");
        }

        System.out.println("REASON    : " + accessDeniedException.getMessage());
        System.out.println("==================================================\n");

        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }
}
