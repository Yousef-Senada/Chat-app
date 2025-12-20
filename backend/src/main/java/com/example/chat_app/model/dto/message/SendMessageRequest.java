package com.example.chat_app.model.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendMessageRequest(
                @NotNull(message = "Chat ID is required") UUID chatId,
                @NotBlank(message = "Message type is required") String messageType,
                String content,
                String mediaUrl) {
}
