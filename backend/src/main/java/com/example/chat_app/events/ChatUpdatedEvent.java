package com.example.chat_app.events;

import com.example.chat_app.model.dto.chat.ChatDisplayDto;
import java.util.UUID;

/**
 * Event published when a chat's properties are updated.
 */
public record ChatUpdatedEvent(
        UUID chatId,
        ChatDisplayDto chat) {
}
