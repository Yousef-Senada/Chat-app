package com.example.chat_app.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Interface for JWT token operations.
 */
public interface IJwtService {

    /**
     * Extract username from JWT token.
     */
    String extractUsername(String token);

    /**
     * Generate a new JWT token for a user.
     */
    String generateToken(UserDetails userDetails);

    /**
     * Validate if a token is valid for the given user.
     */
    boolean isTokenValid(String token, UserDetails userDetails);
}
