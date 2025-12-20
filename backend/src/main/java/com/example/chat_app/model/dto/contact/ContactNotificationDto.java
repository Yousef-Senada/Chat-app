package com.example.chat_app.model.dto.contact;

import java.util.UUID;

public record ContactNotificationDto(
    UUID userId,
    String username,
    String updateType 
) {}
