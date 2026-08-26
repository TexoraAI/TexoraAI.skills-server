package com.lms.live_session.controller;

import com.lms.live_session.dto.CalendarSyncResponseDTO;
import com.lms.live_session.dto.CalendarSyncStatusDTO;
import com.lms.live_session.entity.CalendarSync;
import com.lms.live_session.entity.SyncStatus;
import com.lms.live_session.repository.CalendarSyncRepository;
import com.lms.live_session.service.GoogleCalendarService;
//imports
import jakarta.servlet.http.HttpServletRequest;
import com.lms.live_session.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/calendar-sync")
public class CalendarSyncController {

    private final GoogleCalendarService googleCalendarService;
    private final CalendarSyncRepository calendarSyncRepository;
 // field (add to your existing constructor injection)
    private final JwtUtil jwtUtil;
    // Where to send the browser after OAuth completes. Override with app.base-url.
    @Value("${app.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public CalendarSyncController(GoogleCalendarService googleCalendarService,
                                   CalendarSyncRepository calendarSyncRepository,
                                   JwtUtil jwtUtil) {
        this.googleCalendarService = googleCalendarService;
        this.calendarSyncRepository = calendarSyncRepository;
        this.jwtUtil = jwtUtil;
    }

    // Logged-in frontend calls this WITH a JWT → auth is non-null here.
//    @GetMapping("/auth-url")
//    public ResponseEntity<?> getAuthorizationUrl(Authentication auth) {
//        try {
//            String url = googleCalendarService.generateAuthorizationUrl(auth.getName());
//            return ResponseEntity.ok(Map.of("authUrl", url));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
//        }
//    }
    @GetMapping("/auth-url")
    public ResponseEntity<?> getAuthorizationUrl(HttpServletRequest request,
                                                 @RequestParam(required = false) String returnTo) {
        try {
            String token = resolveBearerToken(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Missing bearer token"));
            }
            String email = jwtUtil.extractEmail(token);
            String role  = jwtUtil.extractRole(token);
            Long orgId   = jwtUtil.extractOrganizationId(token);
            String url = googleCalendarService.generateAuthorizationUrl(email, role, orgId, returnTo);
            return ResponseEntity.ok(Map.of("authUrl", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private static String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    }

    // PUBLIC — Google's redirect has NO JWT, so NO Authentication. Identify the
    // user from `state` only; never touch Authentication here.
//    @GetMapping("/callback")
//    public ResponseEntity<Void> handleOAuthCallback(@RequestParam(required = false) String code,
//                                                    @RequestParam(required = false) String state,
//                                                    @RequestParam(required = false) String error) {
//        if (error != null) {                                   // user declined consent
//            return redirectToApp("error", error);
//        }
//        if (code == null || state == null || state.isBlank()) {
//            return redirectToApp("error", "Missing authorization code or state");
//        }
//        try {
//            googleCalendarService.handleOAuthCallback(code, state);  // state IS the userId
//            return redirectToApp("connected", null);
//        } catch (Exception e) {
//            return redirectToApp("error", e.getMessage());
//        }
//    }
    @GetMapping("/callback")
    public ResponseEntity<Void> handleOAuthCallback(@RequestParam(required = false) String code,
                                                    @RequestParam(required = false) String state,
                                                    @RequestParam(required = false) String error) {
        String returnTo = extractReturnTo(state);
        if (error != null) {
            return redirectToApp(returnTo, "error", error);
        }
        if (code == null || state == null || state.isBlank()) {
            return redirectToApp(returnTo, "error", "Missing authorization code or state");
        }
        try {
            googleCalendarService.handleOAuthCallback(code, state);
            return redirectToApp(returnTo, "connected", null);
        } catch (Exception e) {
            return redirectToApp(returnTo, "error", e.getMessage());
        }
    }
    private static String extractReturnTo(String state) {
        if (state == null || state.isBlank()) return "/";
        String[] parts = state.split("\\|", -1);
        String rt = parts.length > 3 ? parts[3] : "";
        return (rt == null || rt.isBlank()) ? "/" : rt;
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncNow(Authentication auth) {
        try {
            googleCalendarService.syncNow(auth.getName());
            return ResponseEntity.ok(Map.of("message", "Sync started"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<CalendarSyncStatusDTO> getSyncStatus(Authentication auth) {
        Optional<CalendarSync> syncOpt = calendarSyncRepository.findByUserId(auth.getName());
        if (syncOpt.isPresent()) {
            CalendarSync sync = syncOpt.get();
            boolean isConnected = sync.getSyncStatus() == SyncStatus.CONNECTED;
            return ResponseEntity.ok(new CalendarSyncStatusDTO(
                    isConnected, sync.getGoogleEmail(), sync.getLastSyncAt(),
                    isConnected ? "Connected" : "Disconnected"));
        }
        return ResponseEntity.ok(new CalendarSyncStatusDTO());
    }

    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnectCalendar(Authentication auth) {
        try {
            googleCalendarService.disconnectCalendar(auth.getName());
            return ResponseEntity.ok(Map.of("message", "Calendar disconnected"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 302 back to the SPA so the user doesn't land on raw JSON at :9000.
    // ⚠️ Change the path if you have a dedicated Calendar Sync route. The
    // frontend can read ?calendarSync=connected|error to show a toast.
//    private ResponseEntity<Void> redirectToApp(String status, String reason) {
//        StringBuilder target = new StringBuilder(frontendBaseUrl)
//                .append("/?calendarSync=").append(status);
//        if (reason != null) {
//            target.append("&reason=").append(URLEncoder.encode(reason, StandardCharsets.UTF_8));
//        }
//        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(target.toString())).build();
//    }

private ResponseEntity<Void> redirectToApp(String path, String status, String reason) {
    // Only allow same-origin absolute paths (open-redirect guard):
    // must start with a single "/", never "//host" or "http://...".
    String safePath = (path != null && path.startsWith("/") && !path.startsWith("//"))
            ? path : "/";
    StringBuilder target = new StringBuilder(frontendBaseUrl)
            .append(safePath)
            .append("?calendarSync=").append(status);
    if (reason != null) {
        target.append("&reason=")
              .append(URLEncoder.encode(reason, StandardCharsets.UTF_8));
    }
    return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(target.toString())).build();
}
   
}