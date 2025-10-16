package com.example.invoicebackend.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTOs for Template operations.
 */
public class TemplateDtos {

    public static class TemplateCreateRequest {
        @NotNull
        public Long partnerId;

        @NotBlank
        public String name;

        @Schema(description = "Template type", example = "INVOICE")
        public String templateType = "INVOICE";

        public String contentJson;
        public boolean isDefault = false;
    }

    public static class TemplateUpdateRequest {
        public String name;
        public String templateType;
        public String contentJson;
        public Boolean isDefault;
    }

    public static class TemplateResponse {
        public Long id;
        public Long partnerId;
        public String name;
        public String templateType;
        public boolean isDefault;
    }
}
