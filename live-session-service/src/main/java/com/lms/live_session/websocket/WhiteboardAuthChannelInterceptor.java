package com.lms.live_session.websocket;

import com.lms.live_session.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Authenticates STOMP connections at CONNECT time.
 *
 * The whiteboard's REST endpoints (/state, /save, /clear) read the caller's
 * identity from the HTTP Authorization header via HttpServletRequest. That
 * path doesn't exist once a WebSocket connection is already established —
 * individual STOMP frames (SEND, SUBSCRIBE, ...) don't carry their own
 * HttpServletRequest. Instead:
 *
 *   1. The client sends the JWT as a STOMP *native header* named
 *      "Authorization" on the CONNECT frame (not an HTTP header — see the
 *      frontend note below).
 *   2. This interceptor validates it exactly once, when CONNECT arrives,
 *      and stores the resulting email in the STOMP session attributes.
 *   3. Every later frame on that same WebSocket connection shares those
 *      session attributes, so WhiteboardController#syncWhiteboard can read
 *      the caller's email back out via SimpMessageHeaderAccessor without
 *      re-parsing the token on every message.
 *
 * If the token is missing or invalid, we throw from preSend(): Spring's
 * STOMP handling turns that into a STOMP ERROR frame back to the client
 * and closes the session, rather than silently upgrading an
 * unauthenticated connection.
 *
 * Frontend note (stomp.js / @stomp/stompjs example):
 *   const client = new Client({
 *     brokerURL: wsUrl,
 *     connectHeaders: { Authorization: `Bearer ${token}` },
 *     ...
 *   });
 */
@Component
public class WhiteboardAuthChannelInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(WhiteboardAuthChannelInterceptor.class);

    /** Key under which the authenticated caller's email is stored in STOMP session attributes. */
    public static final String SESSION_ATTR_EMAIL = "email";

    private final JwtUtil jwtUtil;

    public WhiteboardAuthChannelInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = extractToken(accessor);
            String email = null;

            if (token != null) {
                try {
                    email = jwtUtil.extractEmail(token);
                } catch (Exception ex) {
                    log.warn("WebSocket CONNECT rejected: token present but failed to parse ({})", ex.getMessage());
                }
            }

            if (email == null || email.isBlank()) {
                log.warn("WebSocket CONNECT rejected: missing or invalid JWT (stompSessionId={})",
                        accessor.getSessionId());
                throw new MessagingException("Unauthorized: missing or invalid token on CONNECT");
            }

            if (accessor.getSessionAttributes() != null) {
                accessor.getSessionAttributes().put(SESSION_ATTR_EMAIL, email);
            }
            log.info("WebSocket CONNECT authenticated: {} (stompSessionId={})", email, accessor.getSessionId());
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        // Native STOMP header on the CONNECT frame — NOT an HTTP header.
        List<String> authHeaders = accessor.getNativeHeader("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            return null;
        }
        String raw = authHeaders.get(0);
        if (raw == null) {
            return null;
        }
        return raw.startsWith("Bearer ") ? raw.substring(7) : raw;
    }
}