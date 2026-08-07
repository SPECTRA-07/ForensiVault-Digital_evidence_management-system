package com.dems.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for local file storage foundation.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String location = "uploads";
    private String casesDir = "cases";
    private String tempDir = "temp";
    private String qrDir = "qr";
}
