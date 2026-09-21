package com.lms.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RazorpayConfig {

    // Bare `new RestTemplate()` has no timeout, so a slow/hanging Razorpay call can
    // pin a request thread indefinitely. Bounded connect + read timeouts turn that
    // into a fast, well-understood failure instead.
    //
    // Using SimpleClientHttpRequestFactory directly (millis-based setters) rather
    // than RestTemplateBuilder's Duration-based fluent API — that API's exact
    // method names/signatures have shifted across Spring Boot versions, while
    // this one has been stable since Spring 4.
    @Bean
    public RestTemplate razorpayRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 3 seconds
        factory.setReadTimeout(5000);    // 5 seconds
        return new RestTemplate(factory);
    }
}