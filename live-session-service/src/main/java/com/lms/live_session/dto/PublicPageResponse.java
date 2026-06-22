package com.lms.live_session.dto;

import com.lms.live_session.entity.BookingPageSettings;
import com.lms.live_session.entity.EventType;

import java.util.List;

/**
 * Returned by the public endpoint GET /public/{slug}.
 * Contains everything the public booking landing page needs to render.
 */
public class PublicPageResponse {

    private BookingPageSettings settings;
    private List<EventType>     eventTypes;

    // ── Constructors ──────────────────────────────────────────────────────────

    public PublicPageResponse() {}

    public PublicPageResponse(BookingPageSettings settings, List<EventType> eventTypes) {
        this.settings   = settings;
        this.eventTypes = eventTypes;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public BookingPageSettings getSettings() { return settings; }
    public void setSettings(BookingPageSettings settings) { this.settings = settings; }

    public List<EventType> getEventTypes() { return eventTypes; }
    public void setEventTypes(List<EventType> eventTypes) { this.eventTypes = eventTypes; }
}