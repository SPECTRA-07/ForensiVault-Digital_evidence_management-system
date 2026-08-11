package com.dems.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Enterprise Production Cross-Origin Resource Sharing (CORS) Configuration.
 * Configures allowed origins, HTTP methods, headers, and credential policies driven by environment variables.
 * Provides CorsConfigurationSource bean for Spring Security 6 integration.
 */
@Slf4j
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOriginsConfig;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String[] origins = Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);

        log.info("Configuring Spring Security CORS policy with allowed origins: {}", Arrays.toString(origins));

        boolean allowWildcard = origins.length == 1 && "*".equals(origins[0]);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(origins));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Correlation-ID", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Content-Disposition", "X-Correlation-ID"));
        configuration.setAllowCredentials(!allowWildcard);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                String[] origins = Arrays.stream(allowedOriginsConfig.split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .toArray(String[]::new);

                boolean allowWildcard = origins.length == 1 && "*".equals(origins[0]);

                registry.addMapping("/**")
                        .allowedOrigins(origins)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("Authorization", "Content-Type", "X-Correlation-ID", "Accept", "Origin", "X-Requested-With")
                        .exposedHeaders("Content-Disposition", "X-Correlation-ID")
                        .allowCredentials(!allowWildcard)
                        .maxAge(3600);
            }
        };
    }
}
