package com.example.chat_app.interfaces;

import com.example.chat_app.model.dto.auth.AuthenticationResponse;
import com.example.chat_app.model.dto.auth.LoginRequest;
import com.example.chat_app.model.dto.auth.RegisterRequest;

/**
 * Interface for authentication operations.
 */
public interface IAuthService {

    /**
     * Register a new user.
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * Login a user and return JWT token.
     */
    AuthenticationResponse login(LoginRequest request);
}
