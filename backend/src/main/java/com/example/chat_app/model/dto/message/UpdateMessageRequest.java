package com.example.chat_app.model.dto.message;

import java.util.UUID;

public record UpdateMessageRequest(
        UUID messageId,
        String newContent
) {}
