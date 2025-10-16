package com.example.invoicebackend.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Minimal protected endpoints to verify JWT and RBAC.
 */
@RestController
@RequestMapping("/api/demo")
@Tag(name = "Demo", description = "Demo endpoints for verifying auth and RBAC")
public class DemoSecureController {

    // PUBLIC_INTERFACE
    @GetMapping("/public")
    @Operation(summary = "Public endpoint", description = "Accessible by anyone")
    public String publicEndpoint() {
        return "Public OK";
    }

    // PUBLIC_INTERFACE
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "User endpoint", description = "Requires USER or ADMIN role")
    public String userEndpoint() {
        return "User OK";
    }

    // PUBLIC_INTERFACE
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin endpoint", description = "Requires ADMIN role")
    public String adminEndpoint() {
        return "Admin OK";
    }
}
