package com.example.chat_app.service;

import com.example.chat_app.exceptions.AppException;
import com.example.chat_app.model.dto.auth.AuthenticationResponse;
import com.example.chat_app.model.dto.auth.LoginRequest;
import com.example.chat_app.model.dto.auth.RegisterRequest;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.repository.UserRepository;
import com.example.chat_app.strategies.AuthenticationStrategy;
import com.example.chat_app.interfaces.IAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService implements IAuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final List<AuthenticationStrategy> strategies;

    public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder,
            JwtService jwtService, List<AuthenticationStrategy> strategies) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.strategies = strategies;
    }

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Input validation
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new AppException("Username is required", HttpStatus.BAD_REQUEST);
        }
        if (request.password() == null || request.password().length() < 6) {
            throw new AppException("Password must be at least 6 characters", HttpStatus.BAD_REQUEST);
        }
        if (request.phoneNumber() == null || !request.phoneNumber().matches("^\\+?[0-9]{10,15}$")) {
            throw new AppException("Invalid phone number format", HttpStatus.BAD_REQUEST);
        }

        // Check for duplicates
        if (userRepo.existsByUsername(request.username())) {
            throw new AppException("Username already taken", HttpStatus.CONFLICT);
        }
        if (userRepo.findByPhoneNumber(request.phoneNumber()).isPresent()) {
            throw new AppException("Phone number already registered", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setName(request.name());
        user.setUsername(request.username());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepo.save(user);
        String jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    @Override
    public AuthenticationResponse login(LoginRequest request) {
        return strategies.stream()
                .filter(strategy -> strategy.supports("JWT"))
                .findFirst()
                .orElseThrow(
                        () -> new AppException("Authentication method not supported", HttpStatus.INTERNAL_SERVER_ERROR))
                .authenticate(request);
    }
}
