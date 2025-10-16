package com.example.invoicebackend.service;

import com.example.invoicebackend.model.Role;
import com.example.invoicebackend.model.User;
import com.example.invoicebackend.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads users from database for Spring Security.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public CustomUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Set<GrantedAuthority> authorities = u.getRoles().stream()
                .map(Role::getName)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .authorities(authorities)
                .accountLocked(!"ACTIVE".equalsIgnoreCase(u.getStatus()))
                .disabled(u.isDeleted())
                .build();
    }
}
