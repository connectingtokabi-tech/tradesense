package com.tradesense.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    public Map<String, Object> getCurrentUser(Authentication authentication) {
        return Map.of(
            "username", authentication.getName(),
            "authenticated", authentication.isAuthenticated(),
            "authorities", authentication.getAuthorities()
        );
    }
}

