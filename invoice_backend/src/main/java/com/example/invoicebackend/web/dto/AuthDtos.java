package com.example.invoicebackend.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTOs for authentication endpoints.
 */
public class AuthDtos {

    public static class RegisterRequest {
        @Schema(description = "User email", example = "user@example.com")
        @NotBlank @Email
        public String email;

        @Schema(description = "Plain text password", example = "P@ssw0rd!")
        @NotBlank
        public String password;

        @Schema(description = "Full name", example = "Jane Doe")
        public String fullName;
    }

    public static class LoginRequest {
        @NotBlank @Email
        public String email;

        @NotBlank
        public String password;
    }

    public static class TokenResponse {
        public String accessToken;
        public String tokenType = "Bearer";
        public long expiresInSeconds;

        public TokenResponse(String accessToken, long expiresInSeconds) {
            this.accessToken = accessToken;
            this.expiresInSeconds = expiresInSeconds;
        }
    }

    public static class RefreshRequest {
        @NotBlank
        public String token;
    }
}
