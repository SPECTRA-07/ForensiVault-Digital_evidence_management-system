package com.dems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for Digital Evidence Management System (DEMS).
 */
@SpringBootApplication
@ConfigurationPropertiesScan("com.dems")
public class DigitalEvidenceManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalEvidenceManagementApplication.class, args);
    }
}
