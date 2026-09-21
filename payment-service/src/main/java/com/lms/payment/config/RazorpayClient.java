package com.lms.payment.config;

import com.lms.payment.exception.PaymentGatewayException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// Thin wrapper around the Razorpay REST API (Basic Auth with key id / key secret).
// Kept dependency-free (no razorpay-java SDK) so it only needs RestTemplate.
@Component
public class RazorpayClient {

    private final RestTemplate restTemplate;
    private final RazorpayProperties properties;

    public RazorpayClient(RestTemplate razorpayRestTemplate, RazorpayProperties properties) {
        this.restTemplate = razorpayRestTemplate;
        this.properties = properties;
    }

    /**
     * Calls POST /orders to create a Razorpay order and returns the raw response body
     * (expected keys: id, amount, currency, status, ...).
     */
    public Map<String, Object> createOrder(Integer amountInPaise, String receipt) {
        Map<String, Object> body = new HashMap<>();
        body.put("amount", amountInPaise);
        body.put("currency", "INR");
        body.put("receipt", receipt);
        body.put("payment_capture", 1);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, authHeaders());

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                    properties.getBaseUrl() + "/orders",
                    HttpMethod.POST,
                    request,
                    Map.class
            ).getBody();

            return response;
        } catch (RestClientException ex) {
            // Covers connect/read timeout, connection refused, DNS failure, and
            // non-2xx responses (RestTemplate throws HttpClientErrorException /
            // HttpServerErrorException, both RestClientException subtypes).
            throw new PaymentGatewayException("Razorpay createOrder call failed", ex);
        }
    }

    /**
     * Calls GET /orders/{orderId} to fetch the current status of an order,
     * used by the reconciliation job.
     */
    public Map<String, Object> fetchOrder(String razorpayOrderId) {
        HttpEntity<Void> request = new HttpEntity<>(authHeaders());

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(
                    properties.getBaseUrl() + "/orders/" + razorpayOrderId,
                    HttpMethod.GET,
                    request,
                    Map.class
            ).getBody();

            return response;
        } catch (RestClientException ex) {
            throw new PaymentGatewayException("Razorpay fetchOrder call failed for orderId: " + razorpayOrderId, ex);
        }
    }

    private HttpHeaders authHeaders() {
        String credentials = properties.getKeyId() + ":" + properties.getKeySecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encoded);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}