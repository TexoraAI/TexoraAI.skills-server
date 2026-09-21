package com.lms.payment.dto;

// Raw webhook body from Razorpay is verified as a raw string against the signature
// header first (see PaymentController); this DTO models the parsed JSON shape used
// once signature verification has succeeded.
public class RazorpayWebhookPayload {

    private String eventId;
    private String event;
    private PayloadEntity payload;

    public RazorpayWebhookPayload() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public PayloadEntity getPayload() {
        return payload;
    }

    public void setPayload(PayloadEntity payload) {
        this.payload = payload;
    }

    public static class PayloadEntity {

        private PaymentEntity payment;

        public PaymentEntity getPayment() {
            return payment;
        }

        public void setPayment(PaymentEntity payment) {
            this.payment = payment;
        }
    }

    public static class PaymentEntity {

        private EntityData entity;

        public EntityData getEntity() {
            return entity;
        }

        public void setEntity(EntityData entity) {
            this.entity = entity;
        }
    }

    public static class EntityData {

        private String id;
        private String orderId;
        private Integer amount;
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
