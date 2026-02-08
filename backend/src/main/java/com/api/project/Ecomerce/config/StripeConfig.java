package com.api.project.Ecomerce.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class StripeConfig {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        log.info("Stripe API initialized with key ending in: ...{}",
                secretKey.length() > 4 ? secretKey.substring(secretKey.length() - 4) : "****");
    }
}
