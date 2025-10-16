package com.example.invoicebackend.security;

import com.example.invoicebackend.security.jwt.JwtAuthenticationFilter;
import com.example.invoicebackend.security.jwt.JwtTokenProvider;
import com.example.invoicebackend.service.CustomUserDetailsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures Spring Security for stateless JWT authentication and RBAC.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@Tag(name = "Security", description = "Spring Security configuration for JWT and RBAC")
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    // PUBLIC_INTERFACE
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt for password hashing
        return new BCryptPasswordEncoder();
    }

    // PUBLIC_INTERFACE
    @Bean
    public UserDetailsService userDetailsService(CustomUserDetailsService customUserDetailsService) {
        /** Provide UserDetailsService backed by our CustomUserDetailsService. */
        return customUserDetailsService;
    }

    // PUBLIC_INTERFACE
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        /** AuthenticationManager using DaoAuthenticationProvider. */
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    // PUBLIC_INTERFACE
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        /** Configure HTTP security with JWT filter and stateless sessions. */
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/health", "/docs", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/demo/public").permitAll()
                // Incoming webhooks should be reachable without JWT; protect via signature verification at controller level
                .requestMatchers(HttpMethod.POST, "/api/webhooks/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
