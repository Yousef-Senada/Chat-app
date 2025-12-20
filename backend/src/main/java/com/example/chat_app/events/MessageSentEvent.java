package com.example.chat_app.events;

import com.example.chat_app.model.dto.message.MessageDisplayDto;
import java.util.UUID;

/**
 * Event published when a message is sent to a chat.
 */
public record MessageSentEvent(
        UUID chatId,
        MessageDisplayDto message) {
}
