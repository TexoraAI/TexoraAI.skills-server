package com.lms.payment.kafka;

// Topic constants for payment-service. Values mirror topics.payment / topics.analytics
// in application.yml so producers/consumers across services stay in sync.
public final class PaymentTopics {

    public static final String PAYMENT_EVENTS_TOPIC = "payment-events";
    public static final String ANALYTICS_EVENTS_TOPIC = "analytics-events";
    public static final String INVOICE_EVENTS_TOPIC = "invoice-events";

    private PaymentTopics() {
    }
}
