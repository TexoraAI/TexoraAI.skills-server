package com.lms.live_session.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import com.lms.live_session.config.GoogleCalendarProperties;
import com.lms.live_session.dto.CalendarSyncResponseDTO;
import com.lms.live_session.entity.CalendarSync;
import com.lms.live_session.entity.Event;
import com.lms.live_session.entity.SyncStatus;
import com.lms.live_session.repository.CalendarSyncRepository;
import com.lms.live_session.repository.EventRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================================
 * GOOGLE OAUTH / CALENDAR API SETUP
 * ============================================================================
 * 1. In https://console.cloud.google.com create (or select) a project.
 * 2. APIs & Services > Library: enable the "Google Calendar API".
 * 3. APIs & Services > OAuth consent screen: configure it and add the scope
 *    used below (CalendarScopes.CALENDAR_READONLY).
 * 4. APIs & Services > Credentials: create an OAuth 2.0 Client ID of type
 *    "Web application". Add an Authorized redirect URI that matches
 *    google.calendar.redirect-uri exactly (scheme + host + port + path),
 *    e.g. http://localhost:8080/api/calendar-sync/callback for local dev,
 *    plus the production HTTPS equivalent.
 * 5. Put the generated Client ID / Secret in application.yml (ideally via
 *    env vars):
 *      google:
 *        calendar:
 *          client-id: ${GOOGLE_CALENDAR_CLIENT_ID}
 *          client-secret: ${GOOGLE_CALENDAR_CLIENT_SECRET}
 *          redirect-uri: ${GOOGLE_CALENDAR_REDIRECT_URI:http://localhost:8080/api/calendar-sync/callback}
 * 6. Frontend flow: send the user to the URL from GET /api/calendar-sync/auth-url.
 *    Google redirects back to GET /api/calendar-sync/callback?code=..., which this
 *    service exchanges for tokens.
 * See SETUP.md for the full walkthrough, pom.xml snippet, and open TODOs.
 * ============================================================================
 */
@Service
@Transactional
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "LMS Live Session Calendar Sync";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);

    private final CalendarSyncRepository calendarSyncRepository;
    private final EventRepository eventRepository;
    private final GoogleCalendarProperties googleCalendarProperties;

    public GoogleCalendarService(CalendarSyncRepository calendarSyncRepository,
                                  EventRepository eventRepository,
                                  GoogleCalendarProperties googleCalendarProperties) {
        this.calendarSyncRepository = calendarSyncRepository;
        this.eventRepository = eventRepository;
        this.googleCalendarProperties = googleCalendarProperties;
    }



//    public String generateAuthorizationUrl(String userId, String role, Long organizationId) {
//        try {
//            GoogleAuthorizationCodeFlow flow = createFlow();
//            return flow.newAuthorizationUrl()
//                    .setRedirectUri(googleCalendarProperties.getRedirectUri())
//                    .setAccessType("offline")
//                    .setState(encodeState(userId, role, organizationId)) // carry identity across the round-trip
//                    .set("prompt", "consent")
//                    .build();
//        } catch (GeneralSecurityException | IOException e) {
//            throw new IllegalStateException("Failed to build Google authorization URL: " + e.getMessage(), e);
//        }
//    }
//
// // The callback is hit by Google's browser redirect with NO JWT, so we can't read
// // the user there. We stamp identity into the OAuth `state` here (where they ARE
// // authenticated) and read it back on return. Format: "email|ROLE|orgId".
// private static String encodeState(String userId, String role, Long organizationId) {
//     return userId + "|" + (role == null ? "" : role) + "|" + (organizationId == null ? "" : organizationId);
// }
    public String generateAuthorizationUrl(String userId, String role,
            Long organizationId, String returnTo) {
try {
GoogleAuthorizationCodeFlow flow = createFlow();
return flow.newAuthorizationUrl()
.setRedirectUri(googleCalendarProperties.getRedirectUri())
.setAccessType("offline")
.setState(encodeState(userId, role, organizationId, returnTo))
.set("prompt", "consent")
.build();
} catch (Exception e) {
throw new RuntimeException("Failed to build Google authorization URL", e);
}
}

private static String encodeState(String userId, String role,
      Long organizationId, String returnTo) {
return userId + "|"
+ (role == null ? "" : role) + "|"
+ (organizationId == null ? "" : organizationId) + "|"
+ (returnTo == null ? "" : returnTo);
}
 

//    public CalendarSyncResponseDTO handleOAuthCallback(String code, String userId) {
//        try {
//            GoogleAuthorizationCodeFlow flow = createFlow();
//            TokenResponse tokenResponse = flow.newTokenRequest(code)
//                    .setRedirectUri(googleCalendarProperties.getRedirectUri())
//                    .execute();
//
//            CalendarSync sync = calendarSyncRepository.findByUserId(userId)
//                    .orElseGet(CalendarSync::new);
//
//            sync.setUserId(userId);
//            sync.setAccessToken(tokenResponse.getAccessToken());
//
//            // Google only sends a refresh_token on first consent (or when prompt=consent
//            // forces re-consent, as above) - never overwrite an existing one with null.
//            if (tokenResponse.getRefreshToken() != null) {
//                sync.setRefreshToken(tokenResponse.getRefreshToken());
//            }
//            if (sync.getRefreshToken() == null) {
//                throw new IllegalStateException(
//                        "Google did not return a refresh token. Revoke prior access at " +
//                        "https://myaccount.google.com/permissions and reconnect.");
//            }
//
//            Long expiresIn = tokenResponse.getExpiresInSeconds();
//            sync.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600));
//            sync.setSyncStatus(SyncStatus.CONNECTED);
//
//            // TODO: wire this up to your real user/organization lookup service.
//            // Event.organizationId / creatorName / creatorRole are NOT NULL, and syncNow()
//            // will fail to save any Event until these are populated on the CalendarSync row.
//            // e.g.: User user = userService.findByEmail(userId);
//            //       sync.setOrganizationId(user.getOrganizationId());
//            //       sync.setCreatorName(user.getFullName());
//            //       sync.setCreatorRole(user.getRole());
//
//            Calendar calendarService = buildCalendarService(sync);
//            String googleEmail = calendarService.calendars().get("primary").execute().getId();
//            sync.setGoogleEmail(googleEmail);
//
//            calendarSyncRepository.save(sync);
//            return mapToDTO(sync);
//        } catch (IOException | GeneralSecurityException e) {
//            throw new IllegalStateException("Google OAuth callback failed: " + e.getMessage(), e);
//        }
//    }
 public CalendarSyncResponseDTO handleOAuthCallback(String code, String state) {
	    // state was stamped at /auth-url: "email|ROLE|orgId"
	    String[] parts = state.split("\\|", -1);
	    String userId = parts[0];                                             // email == principal name
	    String role   = (parts.length > 1 && !parts[1].isBlank()) ? parts[1] : null;
	    Long organizationId = (parts.length > 2 && !parts[2].isBlank()) ? Long.valueOf(parts[2]) : null;

	    try {
	        GoogleAuthorizationCodeFlow flow = createFlow();
	        TokenResponse tokenResponse = flow.newTokenRequest(code)
	                .setRedirectUri(googleCalendarProperties.getRedirectUri())
	                .execute();

	        CalendarSync sync = calendarSyncRepository.findByUserId(userId)
	                .orElseGet(CalendarSync::new);

	        sync.setUserId(userId);
	        sync.setAccessToken(tokenResponse.getAccessToken());

	        if (tokenResponse.getRefreshToken() != null) {
	            sync.setRefreshToken(tokenResponse.getRefreshToken());
	        }
	        if (sync.getRefreshToken() == null) {
	            throw new IllegalStateException(
	                    "Google did not return a refresh token. Revoke prior access at " +
	                    "https://myaccount.google.com/permissions and reconnect.");
	        }

	        Long expiresIn = tokenResponse.getExpiresInSeconds();
	        sync.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600));
	        sync.setSyncStatus(SyncStatus.CONNECTED);

	        // ── Identity for imported events. Event.creatorName / creatorRole are NOT
	        //    NULL; the JWT carries no display name, so creatorName falls back to email.
	        sync.setCreatorRole(role != null ? role : "USER");
	        sync.setCreatorName(userId);          // email — no name claim in the token
	        sync.setOrganizationId(organizationId);

	        Calendar calendarService = buildCalendarService(sync);
	        String googleEmail = calendarService.calendars().get("primary").execute().getId();
	        sync.setGoogleEmail(googleEmail);

	        calendarSyncRepository.save(sync);
	        return mapToDTO(sync);
	    } catch (IOException | GeneralSecurityException e) {
	        throw new IllegalStateException("Google OAuth callback failed: " + e.getMessage(), e);
	    }
	}

    public void syncNow(String userId) {
        CalendarSync sync = calendarSyncRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No calendar connection found for user: " + userId));

        if (sync.getSyncStatus() != SyncStatus.CONNECTED) {
            throw new IllegalStateException("Calendar is not connected for user: " + userId);
        }

        try {
            Calendar calendarService = buildCalendarService(sync);
            DateTime timeMin = new DateTime(Date.from(Instant.now().minus(Duration.ofDays(180))));

            List<com.google.api.services.calendar.model.Event> googleEvents = new ArrayList<>();
            String pageToken = null;
            do {
                Events page = calendarService.events().list("primary")
                        .setTimeMin(timeMin) // last 6 months...
                        .setSingleEvents(true) // expand recurring events into individual instances
                        .setOrderBy("startTime")
                        .setMaxResults(250)
                        .setPageToken(pageToken)
                        .execute(); // ...no timeMax set, so this also picks up all future events
                if (page.getItems() != null) {
                    googleEvents.addAll(page.getItems());
                }
                pageToken = page.getNextPageToken();
            } while (pageToken != null);

            for (com.google.api.services.calendar.model.Event googleEvent : googleEvents) {
                if ("cancelled".equals(googleEvent.getStatus())) {
                    continue;
                }
                upsertEvent(googleEvent, sync);
            }

            sync.setLastSyncAt(LocalDateTime.now());
            sync.setNextSyncAt(LocalDateTime.now().plusHours(1));
            calendarSyncRepository.save(sync);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Google Calendar sync failed for user " + userId + ": " + e.getMessage(), e);
        }
    }

    private void upsertEvent(com.google.api.services.calendar.model.Event googleEvent, CalendarSync sync) {
        String googleEventId = googleEvent.getId();
        Event event = eventRepository.findByGoogleEventId(googleEventId).orElseGet(Event::new);

        event.setGoogleEventId(googleEventId);
        event.setTitle(googleEvent.getSummary() != null ? googleEvent.getSummary() : "(No title)");
        event.setDescription(googleEvent.getDescription());
        event.setMode("ONLINE"); // per spec: Google Calendar events are treated as online
        event.setCreatorId(sync.getUserId());
        event.setCreatorName(sync.getCreatorName());
        event.setCreatorRole(sync.getCreatorRole());
        event.setOrganizationId(sync.getOrganizationId());

        applyEventTimes(event, googleEvent);

        eventRepository.save(event);
    }

    private void applyEventTimes(Event event, com.google.api.services.calendar.model.Event googleEvent) {
        EventDateTime start = googleEvent.getStart();
        EventDateTime end = googleEvent.getEnd();
        if (start == null || end == null) {
            return;
        }

        if (start.getDateTime() != null && end.getDateTime() != null) {
            ZonedDateTime zonedStart = Instant.ofEpochMilli(start.getDateTime().getValue()).atZone(ZoneId.systemDefault());
            ZonedDateTime zonedEnd = Instant.ofEpochMilli(end.getDateTime().getValue()).atZone(ZoneId.systemDefault());
            event.setDate(zonedStart.toLocalDate());
            event.setStartTime(zonedStart.toLocalTime());
            event.setEndTime(zonedEnd.toLocalTime());
        } else if (start.getDate() != null) {
            // all-day event: Google only gives a date, no time component
            LocalDate date = LocalDate.parse(start.getDate().toStringRfc3339());
            event.setDate(date);
            event.setStartTime(LocalTime.MIDNIGHT);
            event.setEndTime(LocalTime.of(23, 59));
        }
    }

//    public void disconnectCalendar(String userId) {
//        CalendarSync sync = calendarSyncRepository.findByUserId(userId)
//                .orElseThrow(() -> new IllegalStateException("No calendar connection found for user: " + userId));
//
//        sync.setSyncStatus(SyncStatus.DISCONNECTED);
//        sync.setRefreshToken(null);
//        sync.setAccessToken(null);
//        sync.setTokenExpiresAt(null);
//        calendarSyncRepository.save(sync);
//    }
    public void disconnectCalendar(String userId) {
        CalendarSync sync = calendarSyncRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No calendar connection found for user: " + userId));

        // refresh_token is NOT NULL, so blanking it and saving throws (that's the 400).
        // Disconnect = drop the stored Google grant entirely → delete the row.
        // Reconnecting rebuilds a fresh row via handleOAuthCallback (prompt=consent
        // makes Google re-issue a refresh token).
        calendarSyncRepository.delete(sync);
    }

    /**
     * Syncs every CONNECTED calendar. Intentionally NOT annotated with @Scheduled here -
     * CalendarSyncScheduler is the single hourly trigger for this. Annotating both would
     * fire this twice per hour and double-sync every calendar.
     */
    public void scheduledSyncAll() {
        List<CalendarSync> syncs = calendarSyncRepository.findBySyncStatus(SyncStatus.CONNECTED);
        for (CalendarSync sync : syncs) {
            try {
                syncNow(sync.getUserId());
            } catch (Exception e) {
                // one bad calendar (revoked token, API outage, etc.) shouldn't stop the batch
                System.err.println("❌ Sync failed for user " + sync.getUserId() + ": " + e.getMessage());
            }
        }
    }

    private Calendar buildCalendarService(CalendarSync sync) throws GeneralSecurityException, IOException {
        Credential credential = buildCredential(sync);
        return new Calendar.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential buildCredential(CalendarSync sync) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(JSON_FACTORY)
                .setClientSecrets(googleCalendarProperties.getClientId(), googleCalendarProperties.getClientSecret())
                .build();

        credential.setRefreshToken(sync.getRefreshToken());
        credential.setAccessToken(sync.getAccessToken());

        boolean expired = sync.getTokenExpiresAt() == null
                || sync.getTokenExpiresAt().isBefore(LocalDateTime.now().plusMinutes(1));

        if (expired) {
            boolean refreshed = credential.refreshToken();
            if (refreshed) {
                sync.setAccessToken(credential.getAccessToken());
                Long expiresIn = credential.getExpiresInSeconds();
                sync.setTokenExpiresAt(LocalDateTime.now().plusSeconds(expiresIn != null ? expiresIn : 3600));
                calendarSyncRepository.save(sync);
            }
        }

        return credential;
    }

    private GoogleAuthorizationCodeFlow createFlow() throws GeneralSecurityException, IOException {
        GoogleClientSecrets.Details details = new GoogleClientSecrets.Details()
                .setClientId(googleCalendarProperties.getClientId())
                .setClientSecret(googleCalendarProperties.getClientSecret());
        GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(details);

        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, clientSecrets, SCOPES)
                .setAccessType("offline")
                .build();
    }

    private CalendarSyncResponseDTO mapToDTO(CalendarSync sync) {
        return new CalendarSyncResponseDTO(
                sync.getId(),
                sync.getGoogleEmail(),
                sync.getSyncStatus() != null ? sync.getSyncStatus().name() : null,
                sync.getLastSyncAt(),
                sync.getNextSyncAt()
        );
    }
}