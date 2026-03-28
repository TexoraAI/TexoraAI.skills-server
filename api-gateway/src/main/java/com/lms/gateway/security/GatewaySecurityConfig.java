
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

            String path = exchange.getRequest().getURI().getPath();

            // ================= MISSING LOGIC #1 =================
            // ✅ Allow CORS preflight
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }
             
            // 🔓 PUBLIC ENDPOINTS
            if (path.startsWith("/api/auth/google")
                    || path.startsWith("/api/auth/login")
                    || path.startsWith("/api/auth/register")
                    || path.startsWith("/api/auth/forgot-password")
                    || path.startsWith("/api/auth/reset-password")
                    || path.startsWith("/api/auth/verify-email")
                    || path.startsWith("/api/student/apply")
                    || path.startsWith("/api/trainer/apply")
                    || path.startsWith("/api/business/apply")
                    || path.startsWith("/api/admin/apply")
                    || path.startsWith("/live-chat")
                    || path.startsWith("/live-chat")
                    || path.startsWith("/api/auth/resend-verification")
                    || path.startsWith("/api/courses/")
                    || path.startsWith("/api/content/student/course/")
                    || path.startsWith("/api/content/course/")
                    ||path.startsWith("/api/files/view/")) {

                return chain.filter(exchange);
            }
         // 🔓 PUBLIC VIDEO GET APIs
            if (
                (path.equals("/api/video") || path.equals("/api/video/"))
                    && exchange.getRequest().getMethod() == HttpMethod.GET
            ) {
                return chain.filter(exchange);
            }

            // 🔓 PUBLIC VIDEO GET BY ID
            if (path.matches("/api/video/\\d+")
                    && exchange.getRequest().getMethod() == HttpMethod.GET) {
                return chain.filter(exchange);
            }

            // 🔓 PUBLIC VIDEO PLAY
            if (path.startsWith("/api/video/play/")
                    && exchange.getRequest().getMethod() == HttpMethod.GET) {
                return chain.filter(exchange);
            }
         // 🔓 PUBLIC FEATURED COURSES (for explore page)
            if (path.startsWith("/api/featured-courses")
                    && exchange.getRequest().getMethod() == HttpMethod.GET) {
                return chain.filter(exchange);
            }

         // ================= PUBLIC UPLOAD COURSE (MOVE HERE) =================

         // 🔓 PUBLIC GET ALL
         if (path.equals("/api/upload-course/all")
                 && exchange.getRequest().getMethod() == HttpMethod.GET) {
             return chain.filter(exchange);
         }

         // 🔓 PUBLIC STREAM
         if (path.startsWith("/api/upload-course/stream/")
                 && exchange.getRequest().getMethod() == HttpMethod.GET) {
             return chain.filter(exchange);
         }

            
            // 🔐 JWT REQUIRED
            String authHeader =
                    exchange.getRequest()
                            .getHeaders()
                            .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            try {
                jwtUtil.validateToken(token);
            } catch (Exception e) {
                exchange.getResponse()
                        .setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = claims.get("role", String.class);

            // ================= MISSING LOGIC #2 =================
            // ✅ SEARCH SERVICE (no role restriction)
            if (path.startsWith("/api/search")) {
                return chain.filter(exchange);
            }
            
            
            // ================= ATTENDANCE SERVICE =================

            // 🧑‍🏫 Trainer Attendance APIs
            if (path.startsWith("/api/trainer/attendance")) {

                if ("TRAINER".equalsIgnoreCase(role)
                        || "ADMIN".equalsIgnoreCase(role)) {
                    return chain.filter(exchange);
                }

                exchange.getResponse()
                        .setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // 🧑‍🎓 Student Attendance APIs
            if (path.startsWith("/api/student/attendance")) {

                if ("STUDENT".equalsIgnoreCase(role)) {
                    return chain.filter(exchange);
                }

                exchange.getResponse()
                        .setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            
            
            
            
            
            

            // ================= STUDENT SERVICE =================
            if (path.startsWith("/api/students")) {

                // Allow ADMIN and TRAINER
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)) {

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            
         // ================= TRAINER SERVICE (✅ MISSING – ADD THIS) =================
            if (path.startsWith("/api/trainers")) {

                // Only ADMIN can manage trainers
                if (!"ADMIN".equalsIgnoreCase(role)) {
                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            
            
            
        
            
            
            
         // ================= VIDEO SERVICE =================
            if (path.startsWith("/api/video")) {

                // ❌ Block students from uploading
                if (path.startsWith("/api/video/upload")) {
                    if (!"TRAINER".equalsIgnoreCase(role)
                            && !"ADMIN".equalsIgnoreCase(role)) {

                        exchange.getResponse()
                                .setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                // ✅ Allow viewing/listing for STUDENT, TRAINER, ADMIN
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
                
                if (path.startsWith("/api/video") && exchange.getRequest().getMethod() == HttpMethod.DELETE) {
                    if (!"TRAINER".equalsIgnoreCase(role)
                            && !"ADMIN".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

            }
            
         // ================= UPLOAD COURSE (VIDEO SERVICE) =================
         // ================= UPLOAD COURSE (VIDEO SERVICE) =================
            if (path.startsWith("/api/upload-course")) {

                // 🔓 SKIP PUBLIC AGAIN (VERY IMPORTANT)
                if (path.equals("/api/upload-course/all")
                        && exchange.getRequest().getMethod() == HttpMethod.GET) {
                    return chain.filter(exchange);
                }

                if (path.startsWith("/api/upload-course/stream/")
                        && exchange.getRequest().getMethod() == HttpMethod.GET) {
                    return chain.filter(exchange);
                }

                // 🔐 POST → ONLY ADMIN
                if (exchange.getRequest().getMethod() == HttpMethod.POST) {
                    if (!"ADMIN".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                // 🔐 DELETE → ONLY ADMIN
                if (exchange.getRequest().getMethod() == HttpMethod.DELETE) {
                    if (!"ADMIN".equalsIgnoreCase(role)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                return chain.filter(exchange);
            }
         // ================= COURSE VIDEO =================
            if (path.startsWith("/api/course-videos")) {

                if (exchange.getRequest().getMethod() == HttpMethod.POST
                        || exchange.getRequest().getMethod() == HttpMethod.DELETE) {

                    if (!"TRAINER".equalsIgnoreCase(role)
                            && !"ADMIN".equalsIgnoreCase(role)) {

                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {

                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            
         // 🔐 FEATURED COURSE CREATE (ADMIN ONLY)
            if (path.startsWith("/api/featured-courses")
                    && exchange.getRequest().getMethod() == HttpMethod.POST) {

                if (!"ADMIN".equalsIgnoreCase(role)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
         

            // ================= ENROLLMENT SERVICE =================
            if (path.startsWith("/api/enrollments")) {

                if ("STUDENT".equalsIgnoreCase(role)
                        && !path.startsWith("/api/enrollments/student")) {
                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            // ================= PROGRESS SERVICE =================
            if (path.startsWith("/api/progress")) {

                if ("STUDENT".equalsIgnoreCase(role)
                        && !path.startsWith("/api/progress/student")) {
                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            
         // ================= CERTIFICATE FILES =================
            if (path.startsWith("/api/files/certificates")) {

                // ✅ Allow certificate generation & download
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

                // ✅ Skip normal file restrictions
                return chain.filter(exchange);
            }

            
            
            
            
         // ================= FILE SERVICE =================
            if (path.startsWith("/api/file")) {

                // ❌ STUDENT CANNOT UPLOAD
                if (path.startsWith("/api/file/upload")) {
                    if (!"ADMIN".equalsIgnoreCase(role)
                            && !"TRAINER".equalsIgnoreCase(role)) {

                        exchange.getResponse()
                                .setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                // ❌ STUDENT CANNOT DELETE
                if (exchange.getRequest().getMethod() == HttpMethod.DELETE) {
                    if (!"ADMIN".equalsIgnoreCase(role)
                            && !"TRAINER".equalsIgnoreCase(role)) {

                        exchange.getResponse()
                                .setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                // ✅ LIST + DOWNLOAD allowed for ADMIN / TRAINER / STUDENT
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }
            
            if (path.startsWith("/api/course-files")) {

                // ❌ STUDENT cannot upload
                if (path.contains("/upload")) {
                    if (!"ADMIN".equalsIgnoreCase(role)
                            && !"TRAINER".equalsIgnoreCase(role)) {

                        exchange.getResponse()
                                .setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                // ❌ STUDENT cannot delete
                if (exchange.getRequest().getMethod() == HttpMethod.DELETE) {
                    if (!"ADMIN".equalsIgnoreCase(role)
                            && !"TRAINER".equalsIgnoreCase(role)) {

                        exchange.getResponse()
                                .setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                }

                // ✅ ADMIN / TRAINER / STUDENT can stream
                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"TRAINER".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

         // ================= ASSESSMENT SERVICE =================
         // ================= ATTEMPT CHECK =================
         // ================= ATTEMPTS =================

         // ✅ Student can check if he already attempted
         if (path.startsWith("/api/attempts/has-attempted")) {

             if ("STUDENT".equalsIgnoreCase(role)
                     && exchange.getRequest().getMethod() == HttpMethod.GET) {
                 return chain.filter(exchange);
             }
//cfjkndjkfnjkn
             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }

         // ✅ Student can submit attempt
         if (path.startsWith("/api/attempts/submit")) {

             if ("STUDENT".equalsIgnoreCase(role)
                     && exchange.getRequest().getMethod() == HttpMethod.POST) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }
         if (path.equals("/api/attempts/my")) {

        	    if ("STUDENT".equalsIgnoreCase(role)
        	            && exchange.getRequest().getMethod() == HttpMethod.GET) {
        	        return chain.filter(exchange);
        	    }

        	    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        	    return exchange.getResponse().setComplete();
        	}

         // 🔐 Trainers/Admins can view attempts
         if (path.startsWith("/api/attempts")) {

             if ("TRAINER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }
     
            // ================= QUIZZES =================
         // ================= QUIZ READ =================
            if (path.matches("/api/quizzes(/.*)?")) {

                // STUDENT can READ quizzes
                if ("STUDENT".equalsIgnoreCase(role)
                        && exchange.getRequest().getMethod() == HttpMethod.GET) {
                    return chain.filter(exchange);
                }

                // ADMIN & TRAINER full access
                if ("ADMIN".equalsIgnoreCase(role)
                        || "TRAINER".equalsIgnoreCase(role)) {
                    return chain.filter(exchange);
                }

                exchange.getResponse()
                        .setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }


            // ================= QUESTIONS / OPTIONS =================
            if (path.startsWith("/api/questions")
                    || path.startsWith("/api/options")) {

                // ❌ Student blocked
                if ("STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

                // ✅ Admin & Trainer
                if ("ADMIN".equalsIgnoreCase(role)
                        || "TRAINER".equalsIgnoreCase(role)) {
                    return chain.filter(exchange);
                }

                exchange.getResponse()
                        .setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
            
         // ================= ASSIGNMENTS =================

         // 🔵 Student can view assignments by batch
         if (path.startsWith("/api/assignments/batch")) {

             if ("STUDENT".equalsIgnoreCase(role)
                     && exchange.getRequest().getMethod() == HttpMethod.GET) {
                 return chain.filter(exchange);
             }

             if ("ADMIN".equalsIgnoreCase(role)
                     || "TRAINER".equalsIgnoreCase(role)) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }

         // 🔵 Trainer/Admin can create assignments
         if (path.equals("/api/assignments")
                 && exchange.getRequest().getMethod() == HttpMethod.POST) {

             if ("ADMIN".equalsIgnoreCase(role)
                     || "TRAINER".equalsIgnoreCase(role)) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }

         // ================= ASSIGNMENT FILES =================

         // 🔵 Students can download files
         if (path.startsWith("/api/assignment-files")
                 && exchange.getRequest().getMethod() == HttpMethod.GET) {

             if ("STUDENT".equalsIgnoreCase(role)
                     || "ADMIN".equalsIgnoreCase(role)
                     || "TRAINER".equalsIgnoreCase(role)) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }

         // 🔵 Trainer/Admin can upload assignment files
         if (path.startsWith("/api/assignment-files")
                 && exchange.getRequest().getMethod() == HttpMethod.POST) {

             if ("ADMIN".equalsIgnoreCase(role)
                     || "TRAINER".equalsIgnoreCase(role)) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }

         // ================= SUBMISSIONS =================

         // 🔵 Student can submit assignment
         if (path.matches("/api/submissions/\\d+")
                 && exchange.getRequest().getMethod() == HttpMethod.POST) {

             if ("STUDENT".equalsIgnoreCase(role)) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }

         // 🔵 Trainer/Admin can view submissions
         if (path.matches("/api/submissions/\\d+")
                 && exchange.getRequest().getMethod() == HttpMethod.GET) {

             if ("ADMIN".equalsIgnoreCase(role)
                     || "TRAINER".equalsIgnoreCase(role)) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }

         // 🔵 Anyone authenticated can download submission
         if (path.startsWith("/api/submissions/download")) {

             if ("STUDENT".equalsIgnoreCase(role)
                     || "ADMIN".equalsIgnoreCase(role)
                     || "TRAINER".equalsIgnoreCase(role)) {
                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }

            
            
            
            
            
         // ================= BATCH SERVICE =================
            
         // ================= BATCH & BRANCH SERVICE (FINAL CLEAN FIX) =================

         // ---------- BRANCH ----------
         if (path.startsWith("/api/branch")) {

             // Only ADMIN can manage branches
             if (!"ADMIN".equalsIgnoreCase(role)) {
                 exchange.getResponse()
                         .setStatusCode(HttpStatus.FORBIDDEN);
                 return exchange.getResponse().setComplete();
             }

             return chain.filter(exchange);
         }

         // ---------- BATCH ----------
         if (path.startsWith("/api/batch")) {

             // STUDENT → only allowed to see own batch
        	 if (path.startsWith("/api/batch/student")) {

        		    if (!role.equalsIgnoreCase("STUDENT")
        		        && !role.equalsIgnoreCase("ROLE_STUDENT")) {

        		        exchange.getResponse()
        		                .setStatusCode(HttpStatus.FORBIDDEN);
        		        return exchange.getResponse().setComplete();
        		    }

        		    return chain.filter(exchange);
        		}

             // TRAINER → trainer batch + reports
          // TRAINER → trainer batch + reports
             if (path.startsWith("/api/batch/trainer")
                     || path.startsWith("/api/batch/reports/trainer")) {

                 if (!"TRAINER".equalsIgnoreCase(role)
                         && !"ADMIN".equalsIgnoreCase(role)) {
                     exchange.getResponse()
                             .setStatusCode(HttpStatus.FORBIDDEN);
                     return exchange.getResponse().setComplete();
                 }

                 return chain.filter(exchange);
             }

             // ADMIN → create / manage batches
             if (!"ADMIN".equalsIgnoreCase(role)) {
                 exchange.getResponse()
                         .setStatusCode(HttpStatus.FORBIDDEN);
                 return exchange.getResponse().setComplete();
             }

             return chain.filter(exchange);
         }
//
//            
//            
        
     
         
               // ================= CHAT SERVICE =================
     

         
//         
//         if (path.startsWith("/api/chat")) {
//
//             // ✅ Allow STUDENT, TRAINER, ADMIN
//             if ("STUDENT".equalsIgnoreCase(role)
//                     || "TRAINER".equalsIgnoreCase(role)
//                     || "ADMIN".equalsIgnoreCase(role)) {
//
//                 return chain.filter(exchange);
//             }
//
//             // ❌ Block anyone else
//             exchange.getResponse()
//                     .setStatusCode(HttpStatus.FORBIDDEN);
//             return exchange.getResponse().setComplete();
//         }
      // ---------- CHAT ----------
         if (path.startsWith("/api/chat")) {

             // STUDENT endpoints
             if (path.startsWith("/api/chat/student")) {
                 if (!role.equalsIgnoreCase("STUDENT")
                         && !role.equalsIgnoreCase("ROLE_STUDENT")) {
                     exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                     return exchange.getResponse().setComplete();
                 }
                 return chain.filter(exchange);
             }

             // TRAINER endpoints
             if (path.startsWith("/api/chat/trainer")) {
                 if (!role.equalsIgnoreCase("TRAINER")
                         && !role.equalsIgnoreCase("ROLE_TRAINER")) {
                     exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                     return exchange.getResponse().setComplete();
                 }
                 return chain.filter(exchange);
             }

             // COMMON endpoints (conversation + send)
             if (path.startsWith("/api/chat/conversation")
                     || path.startsWith("/api/chat/send")) {

                 if (!(role.equalsIgnoreCase("STUDENT")
                         || role.equalsIgnoreCase("TRAINER")
                         || role.equalsIgnoreCase("ROLE_STUDENT")
                         || role.equalsIgnoreCase("ROLE_TRAINER"))) {

                     exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                     return exchange.getResponse().setComplete();
                 }

                 return chain.filter(exchange);
             }
         }

         
         
      // ================= LIVE SESSION SERVICE =================
         if (path.startsWith("/api/live-sessions")
                 || path.startsWith("/api/recordings")
                 || path.startsWith("/api/attendance")) {

             if ("ADMIN".equalsIgnoreCase(role)
                     || "TRAINER".equalsIgnoreCase(role)
                     || "STUDENT".equalsIgnoreCase(role)) {

                 return chain.filter(exchange);
             }

             exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
             return exchange.getResponse().setComplete();
         }
        

       
            // ================= PAYMENT SERVICE =================
            if (path.startsWith("/api/payment")
                    || path.startsWith("/api/refund")) {
 
                if ("STUDENT".equalsIgnoreCase(role)
                        && path.startsWith("/api/refund")) {
                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

                if (!"ADMIN".equalsIgnoreCase(role)
                        && !"STUDENT".equalsIgnoreCase(role)) {
                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }
            }

            return chain.filter(exchange);
        };
    }
}

