package com.convivium.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.stripe")
public class StripeProperties {

    private boolean enabled = false;
    private String secretKey = "";
    private String webhookSecret = "";
    private String successUrl = "http://localhost:5173/c/{condoId}/financial?payment=success";
    private String cancelUrl = "http://localhost:5173/c/{condoId}/financial?payment=cancel";
}
