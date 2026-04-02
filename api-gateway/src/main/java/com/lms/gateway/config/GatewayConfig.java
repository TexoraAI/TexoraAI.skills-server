


package com.lms.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class GatewayConfig {

    @Value("${services.auth}")
    private String authService;

    @Value("${services.user}")
    private String userService;

    @Value("${services.course}")
    private String courseService;

    @Value("${services.content}")
    private String contentService;

    @Value("${services.video}")
    private String videoService;

    @Value("${services.notification}")
    private String notificationService;

    @Value("${services.analytics}")
    private String analyticsService;

    @Value("${services.assessment}")
    private String assessmentService;

    @Value("${services.enrollment}")
    private String enrollmentService;

    @Value("${services.progress}")
    private String progressService;

    @Value("${services.search}")
    private String searchService;

    @Value("${services.payment}")
    private String paymentService;

    @Value("${services.file}")
    private String fileService;

    @Value("${services.student}")
    private String studentService;

    @Value("${services.attendance}")
    private String attendanceService;

    @Value("${services.batch}")
    private String batchService;
    
    @Value("${services.chat}")
    private String chatService;

    @Value("${services.live-session}")
    private String liveSessionService;
    
    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {

        return builder.routes()

//        		.route("live-session-websocket", r -> r
//                        .path("/live-chat/**")
//                        .uri(liveSessionService.replace("http", "ws")))
//        		  // ========== ✅ FIXED: WEBSOCKET ROUTE (rewritePath strips /api prefix) ==========
                .route("live-session-websocket", r -> r
                        .path("/live-chat/**")
                        .filters(f -> f.rewritePath("/live-chat/(?<segment>.*)", "/live-chat/${segment}"))
                        .uri(liveSessionService.replace("http", "ws")))
            // ================= ATTENDANCE SERVICE (✅ FIX: MOVED UP) =================
            .route("attendance-service", r -> r.path(
                    "/api/trainer/attendance/**",
                    "/api/student/attendance/**"
            ).uri(attendanceService))

            // ================= AUTH =================
            .route("auth-service", r -> r.path(
                    "/api/auth/**",
                    "/api/student/**",
                    "/api/trainer/**",
                    "/api/business/**",
                    "/api/admin/**",
                    "/api/organizations/**"
            ).uri(authService))

            // ================= USER =================
            .route("user-service", r -> r.path("/api/users/**")
                    .uri(userService))

            // ================= COURSE =================
//            .route("course-service", r -> r.path("/api/courses/**")
//                    .uri(courseService))
         // ================= COURSE =================
            .route("course-service", r -> r.path(
                    "/api/courses/**",
                    "/api/featured-courses/**"   // ✅ ADD THIS
            ).uri(courseService))
            // ================= CONTENT =================
            .route("content-service", r -> r.path("/api/content/**")
                    .uri(contentService))

          
//         // ================= VIDEO SERVICE =================
//            .route("video-service", r -> r.path(
//                    "/api/video/**",               // 🎬 Video Library
//                    "/api/course-videos/**"        // 📚 Course Module Videos
//            ).uri(videoService))
            .route("video-service", r -> r.path(
                    "/api/video/**",
                    "/api/course-videos/**",
                    "/api/upload-course/**"   // ✅ ADD THIS
            ).uri(videoService))
         
         // ================= VIDEO STATIC FILES =================
            .route("video-static", r -> r.path(
                    "/videos/**"
            ).uri(videoService))
            
            // ================= NOTIFICATION =================
            .route("notification-service", r -> r.path("/api/notification/**")
                    .uri(notificationService))

            // ================= ANALYTICS =================
            .route("analytics-service", r -> r.path("/api/analytics/**")
                    .uri(analyticsService))

            // ================= ASSESSMENT =================
            .route("assessment-service", r -> r.path(
                    "/api/quizzes/**",
                    "/api/questions/**",
                    "/api/options/**",
                    "/api/attempts/**",
                    "/api/assignments/**",          // ✅ ADD THIS
                    "/api/assignment-files/**",     // ✅ ADD THIS
                    "/api/submissions/**" 
            ).uri(assessmentService))

            // ================= ENROLLMENT =================
            .route("enrollment-service", r -> r.path("/api/enrollments/**")
                    .uri(enrollmentService))

            // ================= PROGRESS =================
            .route("progress-service", r -> r.path("/api/progress/**")
                    .uri(progressService))

            // ================= SEARCH =================
            .route("search-service", r -> r.path("/api/search/**")
                    .uri(searchService))

            // ================= PAYMENT =================
            .route("payment-service", r -> r.path(
                    "/api/payment/**",
                    "/api/refund/**"
            ).uri(paymentService))

            // ================= FILE SERVICE =================
            .route("file-service", r -> r.path("/api/file/**")
                    .uri(fileService))

            .route("file-service", r -> r.path(
                    "/api/course-files/**"
            ).uri(fileService))
            
            // ================= STUDENT + TRAINER SERVICE =================
            .route("student-service", r -> r.path(
                    "/api/students/**",
                    "/api/trainers/**"
            ).uri(studentService))

            // ================= BATCH + BRANCH (✅ FIXED) =================
            .route("batch-service", r -> r.path(
                    "/api/batch/**",
                    "/api/branch/**"
            ).uri(batchService))
            
         // ================= CHAT SERVICE (✅ NEW) =================
            .route("chat-service", r -> r.path("/api/chat/**")
                    .uri(chatService))
//         // ================= LIVE SESSION SERVICE =================
//            .route("live-session-service", r -> r.path(
//                    "/api/live-sessions/**",
//                    "/api/recordings/**",
//                    "/api/attendance/**"
//            ).uri(liveSessionService))
//            
//            
//            .build();
         // ================= LIVE SESSION SERVICE =================
            .route("live-session-service", r -> r.path(
                    "/api/live-sessions/**",
                    "/api/recordings/**",
                    "/api/attendance/**"
            ).uri(liveSessionService))
 
            .build();
    }
}
