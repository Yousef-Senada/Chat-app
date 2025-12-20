package com.example.chat_app.events;

import com.example.chat_app.model.dto.chat.ChatDisplayDto;
import java.util.List;

/**
 * Event published when a new chat is created.
 */
public record ChatCreatedEvent(
        ChatDisplayDto chat,
        List<String> usernames // Usernames to notify
) {
}
