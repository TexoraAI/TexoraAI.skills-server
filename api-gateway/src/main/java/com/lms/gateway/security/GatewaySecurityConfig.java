//
//package com.lms.gateway.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//
///**
// * API-Gateway global auth filter.
// *
// * WatchNow access matrix  (/api/v1/watch-now/**)
// * ──────────────────────────────────────────────────
// * GET  /all            PUBLIC  – no token required
// * GET  /stream/**      PUBLIC  – no token required
// * GET  /{id}           PUBLIC  – no token required
// * POST /upload         SUPER_ADMIN only
// * PUT  /**             SUPER_ADMIN only
// * DELETE /**           SUPER_ADMIN only
// *
// * NOTE: The old /api/upload-course/** routes have been REMOVED.
// */
//@Configuration
//public class GatewaySecurityConfig {
//
//    private final JwtUtil jwtUtil;
//    private final SecretKey key;
//
//    public GatewaySecurityConfig(
//            JwtUtil jwtUtil,
//            @Value("${jwt.secret}") String secret
//    ) {
//        this.jwtUtil = jwtUtil;
//        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
//    }
//
//    @Bean
//    public GlobalFilter authenticationFilter() {
//        return (exchange, chain) -> {
//
//            String path   = exchange.getRequest().getURI().getPath();
//            HttpMethod method = exchange.getRequest().getMethod();
//
//            // ── CORS preflight ───────────────────────────────────────────────
//            if (method == HttpMethod.OPTIONS) {
//                return chain.filter(exchange);
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  FULLY PUBLIC ENDPOINTS  (no JWT required)
//            // ════════════════════════════════════════════════════════════════
//
//            // Auth endpoints
//            if (path.startsWith("/api/auth/google")
//                    || path.startsWith("/api/auth/login")
//                    || path.startsWith("/api/auth/register")
//                    || path.startsWith("/api/auth/forgot-password")
//                    || path.startsWith("/api/auth/reset-password")
//                    || path.startsWith("/api/auth/verify-email")
//                    || path.startsWith("/api/auth/check-google")
//                    || path.startsWith("/api/auth/resend-verification")
//                    || path.startsWith("/api/student/apply")
//                    || path.startsWith("/api/trainer/apply")
//                    || path.startsWith("/api/business/apply")
//                    || path.startsWith("/api/admin/apply")) {
//                return chain.filter(exchange);
//            }
//
//            // WebSocket / live
//            if (path.startsWith("/live-chat") || path.startsWith("/ws")) {
//                return chain.filter(exchange);
//            }
//
//            // Public content
//            if (path.startsWith("/api/courses/")
//            		 || path.startsWith("/api/course/v1/featurecourse") 
//            		 || path.startsWith("/api/v1/mentor-feedback/public/")  
//                    || path.startsWith("/api/content/student/course/")
//                    || path.startsWith("/api/content/course/")
//                    || path.startsWith("/api/organizations/")
//                    || path.startsWith("/api/live-sessions/public/")
//                    || path.startsWith("/api/live-sessions/v1/booking/public/")
//                    || path.startsWith("/api/v1/companies/")
//                    || path.startsWith("/api/files/view/")) {
//                return chain.filter(exchange);
//            }
//            
//         // ── Banner Studio — public reads + tracking only ─────────────────
//            if (path.startsWith("/api/banners")) {
//                boolean isPublicGet = method == HttpMethod.GET; // list + getById
//                boolean isTracking = (path.endsWith("/view") || path.endsWith("/click"))
//                        && method == HttpMethod.PATCH;
//                if (isPublicGet || isTracking) {
//                    return chain.filter(exchange);
//                }
//                // everything else (create/update/delete/duplicate/publish/schedule/status/ai-generate)
//                // falls through to JWT requirement below, then gets SUPER_ADMIN-only gated further down.
//            }
//            
//            if ((path.startsWith("/api/v1/cmslandinghubs/public/")
//                    || path.matches("/api/v1/cmslandinghubs/media/\\d+/raw"))
//                    && method == HttpMethod.GET) {
//                return chain.filter(exchange);
//            }
//
//            // Notification newsletter
//            if (path.equals("/api/v1/notification/newsletter/subscribe")
//                    || path.equals("/api/v1/notification/newsletter/unsubscribe")
//                    || path.equals("/api/v1/notification/contact")) {
//                return chain.filter(exchange);
//            }
//         // ── Public Featured Programs (Landing Page) ──────────────────────
////            if (path.startsWith("/api/course/v1/featurecourse")
////                    && method == HttpMethod.GET) {
////                return chain.filter(exchange);
////            }
//
//            // ── Public video library GETs ────────────────────────────────────
//            if ((path.equals("/api/video") || path.equals("/api/video/"))
//                    && method == HttpMethod.GET) {
//                return chain.filter(exchange);
//            }
//            if (path.matches("/api/video/\\d+") && method == HttpMethod.GET) {
//                return chain.filter(exchange);
//            }
//            if (path.startsWith("/api/video/play/") && method == HttpMethod.GET) {
//                return chain.filter(exchange);
//            }
//
//            // ── Public featured courses ──────────────────────────────────────
//            if (path.startsWith("/api/featured-courses") && method == HttpMethod.GET) {
//                return chain.filter(exchange);
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  WATCH NOW – PUBLIC GET endpoints
//            //  (replaces old /api/upload-course public rules)
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/v1/watch-now") && method == HttpMethod.GET) {
//                // Covers: /all  /stream/{file}  /{id}
//                return chain.filter(exchange);
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  JWT REQUIRED FOR EVERYTHING BELOW
//            // ════════════════════════════════════════════════════════════════
//            String authHeader = exchange.getRequest()
//                    .getHeaders()
//                    .getFirst(HttpHeaders.AUTHORIZATION);
//
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                return exchange.getResponse().setComplete();
//            }
//
//            String token = authHeader.substring(7);
//            try {
//                jwtUtil.validateToken(token);
//            } catch (Exception e) {
//                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//                return exchange.getResponse().setComplete();
//            }
//
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            String role = claims.get("role", String.class);
//
//            // ── SUPER_ADMIN: full access to everything ──────────────────────
//            if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
//                return chain.filter(exchange);
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  WATCH NOW – authenticated writes (SUPER_ADMIN already passed)
//            //  Any non-SUPER_ADMIN reaching here is FORBIDDEN.
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/v1/watch-now")) {
//                // POST / PUT / DELETE  →  SUPER_ADMIN only (handled above)
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Search: no role restriction ──────────────────────────────────
//            if (path.startsWith("/api/search")) {
//                return chain.filter(exchange);
//            }
//
//            // ════════════════════════════════════════════════════════════════
////            //  ATTENDANCE
////            // ════════════════════════════════════════════════════════════════
////            if (path.startsWith("/api/trainer/attendance")) {
////                if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
////                    return chain.filter(exchange);
////                }
////                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
////                return exchange.getResponse().setComplete();
////            }
////            if (path.startsWith("/api/student/attendance")) {
////                if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
////                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
////                return exchange.getResponse().setComplete();
////            }
//         // ════════════════════════════════════════════════════════════════
//        //  ATTENDANCE
//        // ════════════════════════════════════════════════════════════════
//        // REPLACE the existing /api/trainer/attendance block with this one.
//        // /api/student/attendance block below it is UNCHANGED.
//
//        if (path.startsWith("/api/trainer/attendance")) {
//            if ("TRAINER".equalsIgnoreCase(role)
//                    || "ADMIN".equalsIgnoreCase(role)
//                    || "TENANT_ADMIN".equalsIgnoreCase(role)
//                    || "SUPER_ADMIN".equalsIgnoreCase(role)) {
//                return chain.filter(exchange);
//            }
//            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//            return exchange.getResponse().setComplete();
//        }
//        if (path.startsWith("/api/student/attendance")) {
//            if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//            return exchange.getResponse().setComplete();
//        }
//
//            // ════════════════════════════════════════════════════════════════
//            //  STUDENTS / TRAINERS management
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/students")) {
//                if (!"ADMIN".equalsIgnoreCase(role) && !"TRAINER".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//            if (path.startsWith("/api/trainers")) {
//                if (!"ADMIN".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  VIDEO LIBRARY
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/video")) {
//                if (path.startsWith("/api/video/upload")) {
//                    if (!"TRAINER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
//                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                        return exchange.getResponse().setComplete();
//                    }
//                }
//                if (!"ADMIN".equalsIgnoreCase(role)
//                        && !"TRAINER".equalsIgnoreCase(role)
//                        && !"STUDENT".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (method == HttpMethod.DELETE) {
//                    if (!"TRAINER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
//                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                        return exchange.getResponse().setComplete();
//                    }
//                }
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  COURSE VIDEOS
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/course-videos")) {
//                if (method == HttpMethod.POST || method == HttpMethod.DELETE) {
//                    if (!"TRAINER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
//                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                        return exchange.getResponse().setComplete();
//                    }
//                }
//                if (!"ADMIN".equalsIgnoreCase(role)
//                        && !"TRAINER".equalsIgnoreCase(role)
//                        && !"STUDENT".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//
//            // ── Featured courses create ──────────────────────────────────────
//            if (path.startsWith("/api/featured-courses") && method == HttpMethod.POST) {
//                if (!"ADMIN".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  ENROLLMENT
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/enrollments")) {
//                if ("STUDENT".equalsIgnoreCase(role)
//                        && !path.startsWith("/api/enrollments/student")) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (!"ADMIN".equalsIgnoreCase(role) && !"STUDENT".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  PROGRESS (reports sub-prefix first)
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/progress/reports")) {
//                if (path.startsWith("/api/progress/reports/batch")) {
//                    if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                } else if (path.startsWith("/api/progress/reports/trainer")) {
//                    if ("ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                }
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.startsWith("/api/progress")) {
//                if ("STUDENT".equalsIgnoreCase(role)) {
//                    if (path.startsWith("/api/progress/mark-complete")
//                            || path.startsWith("/api/progress/user")
//                            || (path.matches("/api/progress/\\d+") && method == HttpMethod.GET)) {
//                        return chain.filter(exchange);
//                    }
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if ("ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Video progress ───────────────────────────────────────────────
//            if (path.startsWith("/api/video-progress")) {
//                if ("STUDENT".equalsIgnoreCase(role)) {
//                    if (path.startsWith("/api/video-progress/user")
//                            || path.startsWith("/api/video-progress/mark-watched")) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if ("ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── File progress ────────────────────────────────────────────────
//            if (path.startsWith("/api/file-progress")) {
//                if ("STUDENT".equalsIgnoreCase(role)) {
//                    if (path.startsWith("/api/file-progress/user")
//                            || path.startsWith("/api/file-progress/mark-downloaded")) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if ("ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Assignment progress ──────────────────────────────────────────
//            if (path.startsWith("/api/assignment-progress")) {
//                if ("STUDENT".equalsIgnoreCase(role)) {
//                    if (path.startsWith("/api/assignment-progress/user")
//                            || path.startsWith("/api/assignment-progress/mark-complete")) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if ("ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Quiz progress ────────────────────────────────────────────────
//            if (path.startsWith("/api/quiz-progress")) {
//                if ("STUDENT".equalsIgnoreCase(role)) {
//                    if (path.startsWith("/api/quiz-progress/user")
//                            || path.startsWith("/api/quiz-progress/mark-attempted")) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if ("ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Skill map ────────────────────────────────────────────────────
//            if (path.startsWith("/api/skill-map")) {
//                if (path.startsWith("/api/skill-map/student")) {
//                    if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                } else if (path.startsWith("/api/skill-map/trainer")) {
//                    if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                } else if (path.startsWith("/api/skill-map/admin")) {
//                    if ("ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                } else if (path.startsWith("/api/skill-map/upsert")) {
//                    return chain.filter(exchange);
//                }
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  CODING / STUDY PLAN
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/v1/code")
//                    || path.startsWith("/api/v1/code-files")
//                    || path.startsWith("/api/v1/problems")
//                    || path.startsWith("/api/v1/assignments")) {
//                if ("STUDENT".equalsIgnoreCase(role)
//                        || "TRAINER".equalsIgnoreCase(role)
//                        || "ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            if (path.startsWith("/api/v1/study-plans")) {
//                if (path.startsWith("/api/v1/study-plans/progress/mark") && method == HttpMethod.POST) {
//                    if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (path.startsWith("/api/v1/study-plans/student")) {
//                    if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Certificate files ────────────────────────────────────────────
//            if (path.startsWith("/api/files/certificates")) {
//                if (!"ADMIN".equalsIgnoreCase(role)
//                        && !"TRAINER".equalsIgnoreCase(role)
//                        && !"STUDENT".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                return chain.filter(exchange);
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            
//         // ════════════════════════════════════════════════════════════════
//            //  FILE SERVICE
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/file")) {
//                if (path.startsWith("/api/file/upload") || method == HttpMethod.DELETE) {
//                    if (!"ADMIN".equalsIgnoreCase(role)
//                            && !"TENANT_ADMIN".equalsIgnoreCase(role)
//                            && !"TRAINER".equalsIgnoreCase(role)) {
//                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                        return exchange.getResponse().setComplete();
//                    }
//                }
//                if (!"ADMIN".equalsIgnoreCase(role)
//                        && !"TENANT_ADMIN".equalsIgnoreCase(role)
//                        && !"TRAINER".equalsIgnoreCase(role)
//                        && !"STUDENT".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//            if (path.startsWith("/api/course-files")) {
//                if (path.contains("/upload") || method == HttpMethod.DELETE) {
//                    if (!"ADMIN".equalsIgnoreCase(role)
//                            && !"TENANT_ADMIN".equalsIgnoreCase(role)
//                            && !"TRAINER".equalsIgnoreCase(role)) {
//                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                        return exchange.getResponse().setComplete();
//                    }
//                }
//                if (!"ADMIN".equalsIgnoreCase(role)
//                        && !"TENANT_ADMIN".equalsIgnoreCase(role)
//                        && !"TRAINER".equalsIgnoreCase(role)
//                        && !"STUDENT".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//            // ════════════════════════════════════════════════════════════════
//            //  ASSESSMENT: ATTEMPTS
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/attempts/has-attempted")) {
//                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.GET) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.startsWith("/api/attempts/submit")) {
//                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.POST) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.equals("/api/attempts/my")) {
//                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.GET) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.startsWith("/api/attempts")) {
//                if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Quizzes ──────────────────────────────────────────────────────
//            if (path.startsWith("/api/quizzes")) {
//                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.GET) return chain.filter(exchange);
//                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Questions / Options ──────────────────────────────────────────
//            if (path.startsWith("/api/questions") || path.startsWith("/api/options")) {
//                if ("STUDENT".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if ("ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Assignments ──────────────────────────────────────────────────
//            if (path.startsWith("/api/assignments/batch")) {
//                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.GET) return chain.filter(exchange);
//                if ("ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.equals("/api/assignments") && method == HttpMethod.POST) {
//                if ("ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.startsWith("/api/assignment-files") && method == HttpMethod.GET) {
//                if ("STUDENT".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.startsWith("/api/assignment-files") && method == HttpMethod.POST) {
//                if ("ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Submissions ──────────────────────────────────────────────────
//            if (path.matches("/api/submissions/\\d+") && method == HttpMethod.POST) {
//                if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.matches("/api/submissions/\\d+") && method == HttpMethod.GET) {
//                if ("ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//            if (path.startsWith("/api/submissions/download")) {
//                if ("STUDENT".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  BATCH / BRANCH / DEPARTMENTS
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/branch")) {
//                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                return chain.filter(exchange);
//            }
//            if (path.startsWith("/api/batch")) {
//                if (path.startsWith("/api/batch/student")) {
//                    if ("STUDENT".equalsIgnoreCase(role) || "ROLE_STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (path.startsWith("/api/departments")) {
//                    if (!"ADMIN".equalsIgnoreCase(role)) {
//                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                        return exchange.getResponse().setComplete();
//                    }
//                    return chain.filter(exchange);
//                }
//                if (path.startsWith("/api/batch/trainer") || path.startsWith("/api/batch/reports/trainer")) {
//                    if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                return chain.filter(exchange);
//            }
//
//            // ════════════════════════════════════════════════════════════════
//            //  CHAT / FEEDBACK / NOTEBOOKS
//            // ════════════════════════════════════════════════════════════════
//            if (path.startsWith("/api/chat")) {
//                if (path.startsWith("/api/chat/student")) {
//                    if ("STUDENT".equalsIgnoreCase(role) || "ROLE_STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (path.startsWith("/api/chat/trainer")) {
//                    if ("TRAINER".equalsIgnoreCase(role) || "ROLE_TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (path.startsWith("/api/chat/conversation") || path.startsWith("/api/chat/send")) {
//                    if ("STUDENT".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)
//                            || "ROLE_STUDENT".equalsIgnoreCase(role) || "ROLE_TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//            if (path.startsWith("/api/feedback")) {
//                if (path.startsWith("/api/feedback/student") || path.equals("/api/feedback/submit")) {
//                    if ("STUDENT".equalsIgnoreCase(role) || "ROLE_STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (path.startsWith("/api/feedback/trainer")) {
//                    if ("TRAINER".equalsIgnoreCase(role) || "ROLE_TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (path.startsWith("/api/feedback/admin")) {
//                    if ("ADMIN".equalsIgnoreCase(role)||"TENANT_ADMIN".equalsIgnoreCase(role))
//                    	return chain.filter(exchange);
//                    
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//            if (path.startsWith("/api/notebooks")) {
//                if ("STUDENT".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)
//                        || "ADMIN".equalsIgnoreCase(role)
//                        || "ROLE_STUDENT".equalsIgnoreCase(role) || "ROLE_TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Live sessions ────────────────────────────────────────────────
//            if (path.startsWith("/api/live-sessions")
//                    || path.startsWith("/api/recordings")
//                    || path.startsWith("/api/attendance")) {
//                if ("ADMIN".equalsIgnoreCase(role)
//                        || "TRAINER".equalsIgnoreCase(role)
//                        || "STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Payment ──────────────────────────────────────────────────────
//            if (path.startsWith("/api/payment") || path.startsWith("/api/refund")) {
//                if ("STUDENT".equalsIgnoreCase(role) && path.startsWith("/api/refund")) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                if (!"ADMIN".equalsIgnoreCase(role) && !"STUDENT".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//            }
//            
//            
//         // ════════════════════════════════════════════════════════════════
//        //  WISHLIST — AUTHENTICATED USERS
//        // ════════════════════════════════════════════════════════════════
//        if (path.startsWith("/api/course/v1/wishlist")) {
//
//            // GET /api/course/v1/wishlist/my
//            if (method == HttpMethod.GET) {
//                return chain.filter(exchange);
//            }
//
//            // POST /api/course/v1/wishlist/toggle/{programId}
//            if (method == HttpMethod.POST) {
//                return chain.filter(exchange);
//            }
//
//            exchange.getResponse().setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
//            return exchange.getResponse().setComplete();
//        }
//            
//
//            // ── AI Companion / Whiteboard ────────────────────────────────────
//            if (path.startsWith("/api/v1/ai-companion")
//                    || path.startsWith("/api/v1/live-sessions")) {
//                if ("ADMIN".equalsIgnoreCase(role)
//                        || "TRAINER".equalsIgnoreCase(role)
//                        || "STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
//                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                return exchange.getResponse().setComplete();
//            }
//
//            // ── Organizations ────────────────────────────────────────────────
//            if (path.startsWith("/api/organizations")) {
//                if (!"ADMIN".equalsIgnoreCase(role)) {
//                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
//                    return exchange.getResponse().setComplete();
//                }
//                return chain.filter(exchange);
//            }
//
//            return chain.filter(exchange);
//        };
//    }
//}

package com.lms.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * API-Gateway global auth filter.
 *
 * WatchNow access matrix  (/api/v1/watch-now/**)
 * ──────────────────────────────────────────────────
 * GET  /all            PUBLIC  – no token required
 * GET  /stream/**      PUBLIC  – no token required
 * GET  /{id}           PUBLIC  – no token required
 * POST /upload         SUPER_ADMIN only
 * PUT  /**             SUPER_ADMIN only
 * DELETE /**           SUPER_ADMIN only
 *
 * NOTE: The old /api/upload-course/** routes have been REMOVED.
 */
@Configuration
public class GatewaySecurityConfig {

    private final JwtUtil jwtUtil;
    private final SecretKey key;

    public GatewaySecurityConfig(
            JwtUtil jwtUtil,
            @Value("${jwt.secret}") String secret
    ) {
        this.jwtUtil = jwtUtil;
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Bean
    public GlobalFilter authenticationFilter() {
        return (exchange, chain) -> {

            String path   = exchange.getRequest().getURI().getPath();
            HttpMethod method = exchange.getRequest().getMethod();

            // ── CORS preflight ───────────────────────────────────────────────
            if (method == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            // ════════════════════════════════════════════════════════════════
            //  FULLY PUBLIC ENDPOINTS  (no JWT required)
            // ════════════════════════════════════════════════════════════════

            // Auth endpoints
            if (path.startsWith("/api/auth/google")
                    || path.startsWith("/api/auth/login")
                    || path.startsWith("/api/auth/register")
                    || path.startsWith("/api/auth/forgot-password")
                    || path.startsWith("/api/auth/reset-password")
                    || path.startsWith("/api/auth/verify-email")
                    || path.startsWith("/api/auth/check-google")
                    || path.startsWith("/api/auth/resend-verification")
                    || path.startsWith("/api/student/apply")
                    || path.startsWith("/api/trainer/apply")
                    || path.startsWith("/api/business/apply")
                    || path.startsWith("/api/admin/apply")) {
                return chain.filter(exchange);
            }

            // WebSocket / live
            if (path.startsWith("/live-chat") || path.startsWith("/ws")) {
                return chain.filter(exchange);
            }

            // Public content
            if (path.startsWith("/api/courses/")
            		 || path.startsWith("/api/course/v1/featurecourse") 
            		 || path.startsWith("/api/v1/mentor-feedback/public/")  
                    || path.startsWith("/api/content/student/course/")
                    || path.startsWith("/api/content/course/")
                    || path.startsWith("/api/organizations/")
                    || path.startsWith("/api/live-sessions/public/")
                    || path.startsWith("/api/live-sessions/v1/booking/public/")
                    || path.startsWith("/api/v1/companies/")
                    || path.startsWith("/api/files/view/")) {
                return chain.filter(exchange);
            }
            
         // ── Banner Studio — public reads + tracking only ─────────────────
            if (path.startsWith("/api/banners")) {
                boolean isPublicGet = method == HttpMethod.GET; // list + getById
                boolean isTracking = (path.endsWith("/view") || path.endsWith("/click"))
                        && method == HttpMethod.PATCH;
                if (isPublicGet || isTracking) {
                    return chain.filter(exchange);
                }
                // everything else (create/update/delete/duplicate/publish/schedule/status/ai-generate)
                // falls through to JWT requirement below, then gets SUPER_ADMIN-only gated further down.
            }
            
            if ((path.startsWith("/api/v1/cmslandinghubs/public/")
                    || path.matches("/api/v1/cmslandinghubs/media/\\d+/raw"))
                    && method == HttpMethod.GET) {
                return chain.filter(exchange);
            }

            // Notification newsletter
            if (path.equals("/api/v1/notification/newsletter/subscribe")
                    || path.equals("/api/v1/notification/newsletter/unsubscribe")
                    || path.equals("/api/v1/notification/contact")) {
                return chain.filter(exchange);
            }
         // ── Public Featured Programs (Landing Page) ──────────────────────
//            if (path.startsWith("/api/course/v1/featurecourse")
//                    && method == HttpMethod.GET) {
//                return chain.filter(exchange);
//            }

            // ── Public video library GETs ────────────────────────────────────
            if ((path.equals("/api/video") || path.equals("/api/video/"))
                    && method == HttpMethod.GET) {
                return chain.filter(exchange);
            }
            if (path.matches("/api/video/\\d+") && method == HttpMethod.GET) {
                return chain.filter(exchange);
            }
            if (path.startsWith("/api/video/play/") && method == HttpMethod.GET) {
                return chain.filter(exchange);
            }

            // ── Public featured courses ──────────────────────────────────────
            if (path.startsWith("/api/featured-courses") && method == HttpMethod.GET) {
                return chain.filter(exchange);
            }

            // ════════════════════════════════════════════════════════════════
            //  WATCH NOW – PUBLIC GET endpoints
            //  (replaces old /api/upload-course public rules)
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/v1/watch-now") && method == HttpMethod.GET) {
                // Covers: /all  /stream/{file}  /{id}
                return chain.filter(exchange);
            }

            // ════════════════════════════════════════════════════════════════
            //  JWT REQUIRED FOR EVERYTHING BELOW
            // ════════════════════════════════════════════════════════════════
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);
            try {
                jwtUtil.validateToken(token);
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);

            // ── SUPER_ADMIN: full access to everything ──────────────────────
            if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
                return chain.filter(exchange);
            }

            // ════════════════════════════════════════════════════════════════
            //  WATCH NOW – authenticated writes (SUPER_ADMIN already passed)
            //  Any non-SUPER_ADMIN reaching here is FORBIDDEN.
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/v1/watch-now")) {
                // POST / PUT / DELETE  →  SUPER_ADMIN only (handled above)
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Search: no role restriction ──────────────────────────────────
            if (path.startsWith("/api/search")) {
                return chain.filter(exchange);
            }

            // ════════════════════════════════════════════════════════════════
        //  ATTENDANCE
        // ════════════════════════════════════════════════════════════════
        if (path.startsWith("/api/trainer/attendance")) {
            if ("TRAINER".equalsIgnoreCase(role)
                    || "ADMIN".equalsIgnoreCase(role)
                    || "TENANT_ADMIN".equalsIgnoreCase(role)
                    || "SUPER_ADMIN".equalsIgnoreCase(role)) {
                return chain.filter(exchange);
            }
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }
        if (path.startsWith("/api/student/attendance")) {
            if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

            // ════════════════════════════════════════════════════════════════
            //  STUDENTS / TRAINERS management
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/students")) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role) && !"TRAINER".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            if (path.startsWith("/api/trainers")) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // ════════════════════════════════════════════════════════════════
            //  VIDEO LIBRARY
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/video")) {
                if (path.startsWith("/api/video/upload")) {
                    if (!"TRAINER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TENANT_ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (method == HttpMethod.DELETE) {
                    if (!"TRAINER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }
            }

            // ════════════════════════════════════════════════════════════════
            //  COURSE VIDEOS
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/course-videos")) {
                if (method == HttpMethod.POST || method == HttpMethod.DELETE) {
                    if (!"TRAINER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TENANT_ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // ── Featured courses create ──────────────────────────────────────
            if (path.startsWith("/api/featured-courses") && method == HttpMethod.POST) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // ════════════════════════════════════════════════════════════════
            //  ENROLLMENT
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/enrollments")) {
                if ("STUDENT".equalsIgnoreCase(role)
                        && !path.startsWith("/api/enrollments/student")) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role) && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // ════════════════════════════════════════════════════════════════
            //  PROGRESS (reports sub-prefix first)
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/progress/reports")) {
                if (path.startsWith("/api/progress/reports/batch")) {
                    if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                } else if (path.startsWith("/api/progress/reports/trainer")) {
                    if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                }
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.startsWith("/api/progress")) {
                if ("STUDENT".equalsIgnoreCase(role)) {
                    if (path.startsWith("/api/progress/mark-complete")
                            || path.startsWith("/api/progress/user")
                            || (path.matches("/api/progress/\\d+") && method == HttpMethod.GET)) {
                        return chain.filter(exchange);
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Video progress ───────────────────────────────────────────────
            if (path.startsWith("/api/video-progress")) {
                if ("STUDENT".equalsIgnoreCase(role)) {
                    if (path.startsWith("/api/video-progress/user")
                            || path.startsWith("/api/video-progress/mark-watched")) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── File progress ────────────────────────────────────────────────
            if (path.startsWith("/api/file-progress")) {
                if ("STUDENT".equalsIgnoreCase(role)) {
                    if (path.startsWith("/api/file-progress/user")
                            || path.startsWith("/api/file-progress/mark-downloaded")) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Assignment progress ──────────────────────────────────────────
            if (path.startsWith("/api/assignment-progress")) {
                if ("STUDENT".equalsIgnoreCase(role)) {
                    if (path.startsWith("/api/assignment-progress/user")
                            || path.startsWith("/api/assignment-progress/mark-complete")) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Quiz progress ────────────────────────────────────────────────
            if (path.startsWith("/api/quiz-progress")) {
                if ("STUDENT".equalsIgnoreCase(role)) {
                    if (path.startsWith("/api/quiz-progress/user")
                            || path.startsWith("/api/quiz-progress/mark-attempted")) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Skill map ────────────────────────────────────────────────────
            if (path.startsWith("/api/skill-map")) {
                if (path.startsWith("/api/skill-map/student")) {
                    if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                } else if (path.startsWith("/api/skill-map/trainer")) {
                    if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                } else if (path.startsWith("/api/skill-map/admin")) {
                    if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                } else if (path.startsWith("/api/skill-map/upsert")) {
                    return chain.filter(exchange);
                }
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ════════════════════════════════════════════════════════════════
            //  CODING / STUDY PLAN
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/v1/code")
                    || path.startsWith("/api/v1/code-files")
                    || path.startsWith("/api/v1/problems")
                    || path.startsWith("/api/v1/assignments")) {
                if ("STUDENT".equalsIgnoreCase(role)
                        || "TRAINER".equalsIgnoreCase(role)
                        || "ADMIN".equalsIgnoreCase(role)
                        || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (path.startsWith("/api/v1/study-plans")) {
                if (path.startsWith("/api/v1/study-plans/progress/mark") && method == HttpMethod.POST) {
                    if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (path.startsWith("/api/v1/study-plans/student")) {
                    if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Certificate files ────────────────────────────────────────────
            if (path.startsWith("/api/files/certificates")) {
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TENANT_ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }

            // ════════════════════════════════════════════════════════════════
            
         // ════════════════════════════════════════════════════════════════
            //  FILE SERVICE
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/file")) {
                if (path.startsWith("/api/file/upload") || method == HttpMethod.DELETE) {
                    if (!"ADMIN".equalsIgnoreCase(role)
                            && !"TENANT_ADMIN".equalsIgnoreCase(role)
                            && !"TRAINER".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TENANT_ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            if (path.startsWith("/api/course-files")) {
                if (path.contains("/upload") || method == HttpMethod.DELETE) {
                    if (!"ADMIN".equalsIgnoreCase(role)
                            && !"TENANT_ADMIN".equalsIgnoreCase(role)
                            && !"TRAINER".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TENANT_ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            // ════════════════════════════════════════════════════════════════
            //  ASSESSMENT: ATTEMPTS
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/attempts/has-attempted")) {
                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.GET) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.startsWith("/api/attempts/submit")) {
                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.POST) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.equals("/api/attempts/my")) {
                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.GET) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.startsWith("/api/attempts")) {
                if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Quizzes ──────────────────────────────────────────────────────
            if (path.startsWith("/api/quizzes")) {
                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.GET) return chain.filter(exchange);
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Questions / Options ──────────────────────────────────────────
            if (path.startsWith("/api/questions") || path.startsWith("/api/options")) {
                if ("STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Assignments ──────────────────────────────────────────────────
            if (path.startsWith("/api/assignments/batch")) {
                if ("STUDENT".equalsIgnoreCase(role) && method == HttpMethod.GET) return chain.filter(exchange);
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.equals("/api/assignments") && method == HttpMethod.POST) {
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.startsWith("/api/assignment-files") && method == HttpMethod.GET) {
                if ("STUDENT".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.startsWith("/api/assignment-files") && method == HttpMethod.POST) {
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Submissions ──────────────────────────────────────────────────
            if (path.matches("/api/submissions/\\d+") && method == HttpMethod.POST) {
                if ("STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.matches("/api/submissions/\\d+") && method == HttpMethod.GET) {
                if ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            if (path.startsWith("/api/submissions/download")) {
                if ("STUDENT".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ════════════════════════════════════════════════════════════════
            //  BATCH / BRANCH / DEPARTMENTS
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/branch")) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }
            if (path.startsWith("/api/batch")) {
                if (path.startsWith("/api/batch/student")) {
                    if ("STUDENT".equalsIgnoreCase(role) || "ROLE_STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (path.startsWith("/api/departments")) {
                    if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                    return chain.filter(exchange);
                }
                if (path.startsWith("/api/batch/trainer") || path.startsWith("/api/batch/reports/trainer")) {
                    if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }

            // ════════════════════════════════════════════════════════════════
            //  CHAT / FEEDBACK / NOTEBOOKS
            // ════════════════════════════════════════════════════════════════
            if (path.startsWith("/api/chat")) {
                if (path.startsWith("/api/chat/student")) {
                    if ("STUDENT".equalsIgnoreCase(role) || "ROLE_STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (path.startsWith("/api/chat/trainer")) {
                    if ("TRAINER".equalsIgnoreCase(role) || "ROLE_TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (path.startsWith("/api/chat/conversation") || path.startsWith("/api/chat/send")) {
                    if ("STUDENT".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)
                            || "ROLE_STUDENT".equalsIgnoreCase(role) || "ROLE_TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            if (path.startsWith("/api/feedback")) {
                if (path.startsWith("/api/feedback/student") || path.equals("/api/feedback/submit")) {
                    if ("STUDENT".equalsIgnoreCase(role) || "ROLE_STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (path.startsWith("/api/feedback/trainer")) {
                    if ("TRAINER".equalsIgnoreCase(role) || "ROLE_TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (path.startsWith("/api/feedback/admin")) {
                    if ("ADMIN".equalsIgnoreCase(role)||"TENANT_ADMIN".equalsIgnoreCase(role))
                    	return chain.filter(exchange);
                    
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            if (path.startsWith("/api/notebooks")) {
                if ("STUDENT".equalsIgnoreCase(role) || "TRAINER".equalsIgnoreCase(role)
                        || "ADMIN".equalsIgnoreCase(role)
                        || "TENANT_ADMIN".equalsIgnoreCase(role)
                        || "ROLE_STUDENT".equalsIgnoreCase(role) || "ROLE_TRAINER".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Live sessions ────────────────────────────────────────────────
            if (path.startsWith("/api/live-sessions")
                    || path.startsWith("/api/recordings")
                    || path.startsWith("/api/attendance")) {
                if ("ADMIN".equalsIgnoreCase(role)
                        || "TENANT_ADMIN".equalsIgnoreCase(role)
                        || "TRAINER".equalsIgnoreCase(role)
                        || "STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Payment ──────────────────────────────────────────────────────
            if (path.startsWith("/api/payment") || path.startsWith("/api/refund")) {
                if ("STUDENT".equalsIgnoreCase(role) && path.startsWith("/api/refund")) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role) && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            
            
         // ════════════════════════════════════════════════════════════════
        //  WISHLIST — AUTHENTICATED USERS
        // ════════════════════════════════════════════════════════════════
        if (path.startsWith("/api/course/v1/wishlist")) {

            // GET /api/course/v1/wishlist/my
            if (method == HttpMethod.GET) {
                return chain.filter(exchange);
            }

            // POST /api/course/v1/wishlist/toggle/{programId}
            if (method == HttpMethod.POST) {
                return chain.filter(exchange);
            }

            exchange.getResponse().setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
            return exchange.getResponse().setComplete();
        }
            

            // ── AI Companion / Whiteboard ────────────────────────────────────
            if (path.startsWith("/api/v1/ai-companion")
                    || path.startsWith("/api/v1/live-sessions")) {
                if ("ADMIN".equalsIgnoreCase(role)
                        || "TENANT_ADMIN".equalsIgnoreCase(role)
                        || "TRAINER".equalsIgnoreCase(role)
                        || "STUDENT".equalsIgnoreCase(role)) return chain.filter(exchange);
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // ── Organizations ────────────────────────────────────────────────
            if (path.startsWith("/api/organizations")) {
                if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            }

            return chain.filter(exchange);
        };
    }
}