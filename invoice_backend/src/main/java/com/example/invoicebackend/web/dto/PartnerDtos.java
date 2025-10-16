package com.example.invoicebackend.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTOs for Partner operations.
 */
public class PartnerDtos {

    public static class PartnerCreateRequest {
        @NotBlank
        @Schema(description = "Partner name", example = "Acme LLC")
        public String name;

        @Schema(description = "Legal name")
        public String legalName;

        public String email;
        public String phone;
        public String addressLine1;
        public String addressLine2;
        public String city;
        public String state;
        public String country;
        public String postalCode;

        @Schema(description = "Owner user id", example = "1")
        public Long ownerUserId;
    }

    public static class PartnerUpdateRequest {
        public String name;
        public String legalName;
        public String email;
        public String phone;
        public String addressLine1;
        public String addressLine2;
        public String city;
        public String state;
        public String country;
        public String postalCode;
    }

    public static class PartnerResponse {
        public Long id;
        public String name;
        public String legalName;
        public String email;
        public String phone;
        public String city;
        public String country;
        public boolean deleted;
    }
}
