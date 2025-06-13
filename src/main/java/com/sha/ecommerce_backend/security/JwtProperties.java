package com.sha.ecommerce_backend.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secret;
    private int expirationMs;
    private String issuer;
    private String header;
    private String prefix;
}
