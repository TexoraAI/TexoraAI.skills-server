package com.lms.live_session.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the google.calendar.* keys from application.yml.
 *
 * Values are trimmed on read: OAuth client-ids/secrets pasted into env vars,
 * .env files or IDE run-configs frequently pick up a stray leading/trailing
 * space, which Google rejects as "invalid_client / OAuth client not found"
 * (the space is sent to Google URL-encoded as a "+"). Trimming here guarantees
 * a clean value regardless of how it was injected.
 */
@Component
@ConfigurationProperties(prefix = "google.calendar")
public class GoogleCalendarProperties {

    private String clientId;
    private String clientSecret;
    private String redirectUri;

    public String getClientId() { return clientId == null ? null : clientId.trim(); }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret == null ? null : clientSecret.trim(); }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getRedirectUri() { return redirectUri == null ? null : redirectUri.trim(); }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
}