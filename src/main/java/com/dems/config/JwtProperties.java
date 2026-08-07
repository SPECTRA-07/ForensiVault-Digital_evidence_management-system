package com.dems.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT authentication token generation and validation.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private long expirationMs = 86400000L; // 24 Hours
}
