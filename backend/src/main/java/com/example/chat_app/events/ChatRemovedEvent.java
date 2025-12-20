package com.example.chat_app.events;

import java.util.UUID;

/**
 * Event published when a user is removed from a chat.
 */
public record ChatRemovedEvent(
        UUID chatId,
        String username // Username to notify
) {
}
