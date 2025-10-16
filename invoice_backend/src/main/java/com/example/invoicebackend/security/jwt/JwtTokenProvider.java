package com.example.invoicebackend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

/**
 * Utility for creating and validating JWT tokens.
 */
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.exp.minutes:60}")
    private long jwtExpMinutes;

    private Key getSigningKey() {
        // Ensure at least 256-bit secret for HS256
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // PUBLIC_INTERFACE
    public String generateToken(String subject, List<String> roles) {
        /** Generates a signed JWT with subject and roles, exp from configuration. */
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtExpMinutes * 60_000);
        return Jwts.builder()
                .setSubject(subject)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // PUBLIC_INTERFACE
    public Jws<Claims> parse(String token) throws JwtException {
        /** Parses and validates a JWT token returning the claims if valid. */
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
    }
}
