package com.example.invoicebackend.service;

import com.example.invoicebackend.model.Role;
import com.example.invoicebackend.model.User;
import com.example.invoicebackend.repository.RoleRepository;
import com.example.invoicebackend.repository.UserRepository;
import com.example.invoicebackend.security.jwt.JwtTokenProvider;
import com.example.invoicebackend.web.dto.AuthDtos.LoginRequest;
import com.example.invoicebackend.web.dto.AuthDtos.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

/**
 * Handles authentication logic and admin bootstrap.
 */
@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.jwt.exp.minutes:60}")
    private long jwtExpMinutes;

    public AuthService(AuthenticationManager authManager, UserRepository users,
                       RoleRepository roles, PasswordEncoder encoder, JwtTokenProvider jwt) {
        this.authManager = authManager;
        this.users = users;
        this.roles = roles;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    // PUBLIC_INTERFACE
    public String register(RegisterRequest req) {
        /** Register a standard USER with hashed password. */
        if (users.existsByEmail(req.email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User u = new User();
        u.setEmail(req.email.toLowerCase());
        u.setPasswordHash(encoder.encode(req.password));
        u.setFullName(req.fullName);
        Role userRole = roles.findByName("USER").orElseThrow(() -> new IllegalStateException("USER role missing"));
        u.getRoles().add(userRole);
        users.save(u);
        return issueToken(u.getEmail(), List.of("USER"));
    }

    // PUBLIC_INTERFACE
    public String login(LoginRequest req) {
        /** Authenticate credentials and issue JWT. */
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email, req.password));
        } catch (AuthenticationException ex) {
            throw new BadCredentialsException("Invalid credentials");
        }
        User u = users.findByEmail(req.email).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        List<String> roleNames = u.getRoles().stream().map(Role::getName).toList();
        return issueToken(u.getEmail(), roleNames);
    }

    // PUBLIC_INTERFACE
    public String refresh(String token) {
        /** For MVP, accept current valid token and issue a new one with same subject/roles. */
        var jws = jwt.parse(token);
        String sub = jws.getBody().getSubject();
        @SuppressWarnings("unchecked")
        List<String> rolesFromToken = (List<String>) jws.getBody().get("roles", List.class);
        return issueToken(sub, rolesFromToken);
    }

    private String issueToken(String email, List<String> roles) {
        return jwt.generateToken(email, roles);
    }

    @PostConstruct
    public void seedAdminIfMissing() {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            return; // skip if not configured
        }
        String email = adminEmail.toLowerCase();
        if (!users.existsByEmail(email)) {
            User admin = new User();
            admin.setEmail(email);
            admin.setPasswordHash(encoder.encode(adminPassword));
            admin.setFullName("System Administrator");
            Role adminRole = roles.findByName("ADMIN")
                    .orElseGet(() -> roles.save(new Role("ADMIN")));
            admin.getRoles().add(adminRole);
            users.save(admin);
        }
    }
}
