package com.example.invoicebackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration with HTTP Bearer security scheme for JWT.
 */
@Configuration
public class OpenApiConfig {

    // PUBLIC_INTERFACE
    @Bean
    public OpenAPI api() {
        /** Configure JWT Bearer security scheme globally. */
        final String schemeName = "bearerAuth";
        final SecurityScheme scheme = new SecurityScheme()
                .name(schemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(schemeName, scheme))
                .info(new Info()
                        .title("Invoice Backend API")
                        .version("0.1.0")
                        .description("Smart Invoice & Payment Reminder System - Backend"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .addTagsItem(new Tag().name("Authentication").description("Auth endpoints for registration, login, and refresh"))
                .addTagsItem(new Tag().name("Partners").description("Partner management endpoints"))
                .addTagsItem(new Tag().name("Templates").description("Template CRUD endpoints"))
                .addTagsItem(new Tag().name("Invoices").description("Invoice CRUD and search endpoints"))
                .addTagsItem(new Tag().name("Demo").description("Demo endpoints for verifying auth and RBAC"))
                .addTagsItem(new Tag().name("Security").description("Spring Security configuration for JWT and RBAC"));
    }
}
