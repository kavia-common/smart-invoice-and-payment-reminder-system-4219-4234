package com.example.invoicebackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Provides CORS configuration based on environment variables.
 * Reads comma-separated origins from app.cors.allowed-origins (maps to CORS_ALLOWED_ORIGINS).
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    // PUBLIC_INTERFACE
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        /**
         * PUBLIC_INTERFACE
         * Returns a CorsConfigurationSource configured with allowed origins, headers, and methods.
         * - Origins: read from app.cors.allowed-origins (CORS_ALLOWED_ORIGINS env), supports comma-separated values.
         * - Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
         * - Headers: common auth and content headers
         * - Credentials: allowed to support cookies/Authorization headers
         */
        CorsConfiguration config = new CorsConfiguration();

        // Parse allowed origins (comma-separated). Default '*' if not provided.
        List<String> origins;
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            origins = List.of("*");
        } else {
            origins = Arrays.stream(allowedOrigins.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name()
        ));
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "X-Auth-Token"
        ));
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Disposition"
        ));
        config.setAllowCredentials(true);
        // Keep preflight response cache for a while
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to all paths
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
