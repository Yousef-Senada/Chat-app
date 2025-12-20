package com.example.chat_app.utils;

import com.example.chat_app.model.dto.auth.AuthenticationResponse;
import com.example.chat_app.model.dto.auth.LoginRequest;
import com.example.chat_app.repository.UserRepository;
import com.example.chat_app.service.JwtService;
import com.example.chat_app.strategies.AuthenticationStrategy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthStrategy implements AuthenticationStrategy {
    private AuthenticationManager authenticationManager;
    private UserRepository userRepo;
    private JwtService jwtService;

    public JwtAuthStrategy(AuthenticationManager authenticationManager, UserRepository userRepo, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    @Override
    public AuthenticationResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        var user = userRepo.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    @Override
    public boolean supports(String authType) {
        return "JWT".equalsIgnoreCase(authType);
    }
}
