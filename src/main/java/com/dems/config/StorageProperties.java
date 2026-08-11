package com.dems.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized configuration properties for DEMS storage engine (Local & S3-compatible persistent object storage).
 * Credentials (access key & secret key) are resolved securely via AWS SDK EnvironmentVariableCredentialsProvider / DefaultCredentialsProvider.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private String provider = "local";
    private String bucket = "dems-evidence";
    private String region = "us-east-1";
    private String endpoint;
    private String basePath = "uploads";

    private String location = "uploads";
    private String casesDir = "cases";
    private String tempDir = "temp";
    private String qrDir = "qr";
}
