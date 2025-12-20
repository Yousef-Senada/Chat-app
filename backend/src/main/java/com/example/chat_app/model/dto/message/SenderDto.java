package com.example.chat_app.model.dto.message;

import java.util.UUID;

public record SenderDto(
        UUID senderId,
        String username
) {}
