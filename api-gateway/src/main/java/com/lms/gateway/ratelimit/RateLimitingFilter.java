package com.lms.gateway.ratelimit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    // ── Sentinel result used when Redis is down and fail-open is true
    private static final RateLimitResult FAIL_OPEN_RESULT = new RateLimitResult(true, -1, -1);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<List> rateLimitScript;
    private final SecretKey jwtKey;
    private final boolean failOpen;

    public RateLimitingFilter(
            ReactiveStringRedisTemplate redisTemplate,
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${rate-limit.fail-open:true}") boolean failOpen
    ) {
        this.redisTemplate = redisTemplate;
        this.jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.failOpen = failOpen;

        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("scripts/token_bucket.lua")));
        script.setResultType(List.class);
        this.rateLimitScript = script;
    }

    @Override
    public int getOrder() {
        return -5;
    }

    // ─────────────────────────────────────────────────────────────
    //  Main filter — reactive flow is the KEY fix here
    //
    //  Structure:
    //    checkRateLimit(...)          ← Redis-only Mono
    //      .onErrorResume(...)        ← catches ONLY Redis/Lua errors
    //      .flatMap(result -> ...)    ← chain.filter() is OUTSIDE onErrorResume scope
    //
    //  This ensures downstream Connection refused (course-service, etc.)
    //  is never caught by onErrorResume and never logged as a Redis error.
    // ─────────────────────────────────────────────────────────────
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path   = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod() != null
                ? exchange.getRequest().getMethod().name() : "GET";

        // ── Skip: OPTIONS preflight
        if (HttpMethod.OPTIONS.matches(method)) {
            return chain.filter(exchange);
        }

        // ── Skip: WebSocket upgrade
        String upgradeHeader = exchange.getRequest().getHeaders().getFirst("Upgrade");
        if ("websocket".equalsIgnoreCase(upgradeHeader)) {
            return chain.filter(exchange);
        }

        // ── Skip: WebSocket / static video / live-chat paths
        if (path.startsWith("/ws") || path.startsWith("/live-chat") || path.startsWith("/videos")) {
            return chain.filter(exchange);
        }

        // ── Only rate-limit /api/** paths
        if (!path.startsWith("/api")) {
            return chain.filter(exchange);
        }

        RateLimitGroup group = resolveGroup(path, method);
        String rateLimitKey  = buildKey(exchange, group.name());

        // ── FIXED reactive flow:
        //    1. checkRateLimit emits RateLimitResult (Redis-only work)
        //    2. onErrorResume catches ONLY errors from step 1 (Redis/Lua failures)
        //    3. flatMap runs AFTER onErrorResume is resolved — chain.filter() lives here
        //       so downstream errors propagate normally to Spring Cloud Gateway
        return checkRateLimit(rateLimitKey, group)
                .onErrorResume(redisError -> {
                    // Only reached if Redis itself failed (connection, Lua script, timeout, etc.)
                    // Never reached for downstream service errors because chain.filter()
                    // is called in flatMap below, which is outside this onErrorResume scope.
                    if (failOpen) {
                        log.warn("[RateLimit] Redis unavailable, failing open. key={} error={}",
                                rateLimitKey, redisError.getMessage());
                        return Mono.just(FAIL_OPEN_RESULT);
                    }
                    log.error("[RateLimit] Redis unavailable, failing closed. key={}", rateLimitKey, redisError);
                    return Mono.error(redisError);
                })
                .flatMap(result -> {
                    // ── Rejected: return 429 immediately, no downstream call
                    if (!result.allowed()) {
                        long now       = Instant.now().getEpochSecond();
                        long retryAfter = Math.max(0L, result.resetEpoch() - now);
                        exchange.getResponse().getHeaders()
                                .set("X-RateLimit-Limit",     String.valueOf(group.burstCapacity()));
                        exchange.getResponse().getHeaders()
                                .set("X-RateLimit-Remaining", "0");
                        exchange.getResponse().getHeaders()
                                .set("X-RateLimit-Reset",     String.valueOf(result.resetEpoch()));
                        exchange.getResponse().getHeaders()
                                .set("Retry-After",           String.valueOf(retryAfter));
                        return writeTooManyRequests(exchange);
                    }

                    // ── Allowed: set headers (skip sentinel -1 values for fail-open)
                    if (result.remaining() >= 0) {
                        exchange.getResponse().getHeaders()
                                .set("X-RateLimit-Limit",     String.valueOf(group.burstCapacity()));
                        exchange.getResponse().getHeaders()
                                .set("X-RateLimit-Remaining", String.valueOf(result.remaining()));
                        exchange.getResponse().getHeaders()
                                .set("X-RateLimit-Reset",     String.valueOf(result.resetEpoch()));
                    }

                    // ── chain.filter() is here — completely outside onErrorResume scope.
                    //    Any downstream error (Connection refused to course-service, etc.)
                    //    propagates up naturally and is handled by Spring Cloud Gateway,
                    //    NOT caught by the onErrorResume above.
                    return chain.filter(exchange);
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  Redis-only Mono — emits RateLimitResult or errors with
    //  a Redis/Lua exception. Nothing downstream here.
    // ─────────────────────────────────────────────────────────────
    private Mono<RateLimitResult> checkRateLimit(String rateLimitKey, RateLimitGroup group) {
        long now  = Instant.now().getEpochSecond();
        List<String> keys = List.of(rateLimitKey);
        List<String> args = List.of(
                String.valueOf(group.replenishRate()),
                String.valueOf(group.burstCapacity()),
                String.valueOf(now),
                "1"
        );

        return redisTemplate.execute(rateLimitScript, keys, args)
                .next()
                .map(result -> {
                    @SuppressWarnings("unchecked")
                    List<Long> results = (List<Long>) result;
                    boolean allowed    = results.get(0) == 1L;
                    long remaining     = results.get(1);
                    long resetEpoch    = results.get(2);
                    return new RateLimitResult(allowed, remaining, resetEpoch);
                });
    }

    // ─────────────────────────────────────────────────────────────
    //  Immutable result record
    // ─────────────────────────────────────────────────────────────
    private record RateLimitResult(boolean allowed, long remaining, long resetEpoch) {}

    // ─────────────────────────────────────────────────────────────
    //  Route group resolution — unchanged from original
    // ─────────────────────────────────────────────────────────────
    private RateLimitGroup resolveGroup(String path, String method) {

        if (path.equals("/api/auth/login"))                                    return RateLimitGroup.AUTH_LOGIN;
        if (path.equals("/api/auth/register"))                                 return RateLimitGroup.AUTH_REGISTER;
        if (path.startsWith("/api/auth/forgot-password")
         || path.startsWith("/api/auth/reset-password")
         || path.startsWith("/api/auth/resend-verification"))                  return RateLimitGroup.AUTH_PASSWORD;

        if (path.equals("/api/student/apply")
         || path.equals("/api/trainer/apply")
         || path.equals("/api/business/apply")
         || path.equals("/api/admin/apply"))                                   return RateLimitGroup.PUBLIC_APPLY;

        if (path.equals("/api/quizzes/upload-bulk"))                           return RateLimitGroup.BULK_UPLOAD;

        if (path.equals("/api/video/upload"))                                  return RateLimitGroup.VIDEO_UPLOAD;
        if ("POST".equalsIgnoreCase(method)
         && path.startsWith("/api/upload-course"))                             return RateLimitGroup.VIDEO_UPLOAD;

        if (path.startsWith("/api/file/upload"))                               return RateLimitGroup.FILE_UPLOAD;
        if (path.contains("/upload") && path.startsWith("/api/course-files")) return RateLimitGroup.FILE_UPLOAD;

        if (path.startsWith("/api/v1/ai-companion"))                           return RateLimitGroup.AI_COMPANION;
        if (path.startsWith("/api/v1/live-sessions"))                          return RateLimitGroup.WHITEBOARD_REST;

        if (path.startsWith("/api/v1/code")
         || path.startsWith("/api/v1/code-files")
         || path.startsWith("/api/v1/problems")
         || path.startsWith("/api/v1/assignments"))                            return RateLimitGroup.CODE_API;

        if (path.startsWith("/api/v1/study-plans"))                            return RateLimitGroup.STUDY_PLAN_API;

        if (path.startsWith("/api/payment")
         || path.startsWith("/api/refund"))                                    return RateLimitGroup.PAYMENT_API;

        if (path.startsWith("/api/search"))                                    return RateLimitGroup.SEARCH_API;
        if (path.startsWith("/api/analytics"))                                 return RateLimitGroup.ANALYTICS_API;

        if (path.startsWith("/api/quizzes")
         || path.startsWith("/api/questions")
         || path.startsWith("/api/options")
         || path.startsWith("/api/attempts"))                                  return RateLimitGroup.ASSESSMENT_API;

        if (path.startsWith("/api/assignments")
         || path.startsWith("/api/assignment-files")
         || path.startsWith("/api/submissions"))                               return RateLimitGroup.ASSIGNMENT_API;

        if (path.startsWith("/api/progress")
         || path.startsWith("/api/video-progress")
         || path.startsWith("/api/file-progress")
         || path.startsWith("/api/assignment-progress")
         || path.startsWith("/api/quiz-progress")
         || path.startsWith("/api/skill-map"))                                 return RateLimitGroup.PROGRESS_API;

        if (path.startsWith("/api/enrollments"))                               return RateLimitGroup.ENROLLMENT_API;

        if (path.startsWith("/api/video")
         || path.startsWith("/api/course-videos"))                             return RateLimitGroup.VIDEO_API;

        if (path.startsWith("/api/content"))                                   return RateLimitGroup.CONTENT_API;

        if (path.startsWith("/api/courses")
         || path.startsWith("/api/featured-courses"))                          return RateLimitGroup.COURSE_API;

        if (path.startsWith("/api/users")
         || path.startsWith("/api/v1/resume"))                                 return RateLimitGroup.USER_API;

        if (path.startsWith("/api/notification"))                              return RateLimitGroup.NOTIFICATION_API;

        if (path.startsWith("/api/file")
         || path.startsWith("/api/course-files"))                              return RateLimitGroup.FILE_API;

        if (path.startsWith("/api/students")
         || path.startsWith("/api/trainers"))                                  return RateLimitGroup.STUDENT_API;

        if (path.startsWith("/api/trainer/attendance")
         || path.startsWith("/api/student/attendance"))                        return RateLimitGroup.ATTENDANCE_API;

        if (path.startsWith("/api/batch")
         || path.startsWith("/api/branch"))                                    return RateLimitGroup.BATCH_API;

        if (path.startsWith("/api/chat")
         || path.startsWith("/api/feedback")
         || path.startsWith("/api/notebooks"))                                 return RateLimitGroup.CHAT_API;

        if (path.startsWith("/api/live-sessions")
         || path.startsWith("/api/recordings")
         || path.startsWith("/api/attendance"))                                return RateLimitGroup.LIVE_SESSION_API;

        if (path.startsWith("/api/organizations"))                             return RateLimitGroup.ORGANIZATION_API;

        if (path.startsWith("/api/auth")
         || path.startsWith("/api/student")
         || path.startsWith("/api/trainer")
         || path.startsWith("/api/business")
         || path.startsWith("/api/admin"))                                     return RateLimitGroup.GENERAL_API;

        return RateLimitGroup.GENERAL_API;
    }

    // ─────────────────────────────────────────────────────────────
    //  Key builder — unchanged from original
    // ─────────────────────────────────────────────────────────────
    private String buildKey(ServerWebExchange exchange, String groupName) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtKey)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                String subject = claims.getSubject();
                if (subject != null && !subject.isBlank()) {
                    return "rl:user:" + subject + ":" + groupName;
                }
            } catch (Exception ignored) {
                // JWT invalid — fall through to IP key
            }
        }
        return "rl:ip:" + resolveClientIp(exchange) + ":" + groupName;
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        if (exchange.getRequest().getRemoteAddress() != null) {
            return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    // ─────────────────────────────────────────────────────────────
    //  429 response writer — unchanged from original
    // ─────────────────────────────────────────────────────────────
    private Mono<Void> writeTooManyRequests(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"status\":429,\"error\":\"Too Many Requests\","
                    + "\"message\":\"Rate limit exceeded. Please try again later.\"}";
        var buffer = exchange.getResponse()
                .bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}