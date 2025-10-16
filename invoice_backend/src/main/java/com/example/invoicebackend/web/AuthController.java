package com.example.invoicebackend.web;

import com.example.invoicebackend.service.AuthService;
import com.example.invoicebackend.web.dto.AuthDtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints for registration, login, and token refresh.
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Authentication", description = "Auth endpoints for registration, login, and refresh")
public class AuthController {

    private final AuthService authService;

    @Value("${app.jwt.exp.minutes:60}")
    private long expMinutes;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // PUBLIC_INTERFACE
    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user and return a JWT token")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        String token = authService.register(request);
        return ResponseEntity.ok(new TokenResponse(token, expMinutes * 60));
    }

    // PUBLIC_INTERFACE
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with email/password and obtain a JWT token")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new TokenResponse(token, expMinutes * 60));
    }

    // PUBLIC_INTERFACE
    @PostMapping("/refresh")
    @Operation(summary = "Refresh", description = "Refresh an existing valid JWT token")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        String token = authService.refresh(request.token);
        return ResponseEntity.ok(new TokenResponse(token, expMinutes * 60));
    }
}
