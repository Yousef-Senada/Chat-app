package com.example.chat_app.model.dto.user;

import java.util.UUID;

public record UserResponse(
        UUID ID,
        String name,
        String username,
        String phoneNumber
) {}
