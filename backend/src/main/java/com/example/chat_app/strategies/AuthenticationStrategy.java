package com.example.chat_app.strategies;

import com.example.chat_app.model.dto.auth.AuthenticationResponse;
import com.example.chat_app.model.dto.auth.LoginRequest;

public interface AuthenticationStrategy {
    AuthenticationResponse authenticate(LoginRequest request);

    boolean supports(String authType);
}
