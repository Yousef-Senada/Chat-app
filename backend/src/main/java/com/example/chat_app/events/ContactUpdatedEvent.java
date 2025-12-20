package com.example.chat_app.events;

import com.example.chat_app.model.dto.contact.ContactNotificationDto;

/**
 * Event published when a contact is added/removed.
 */
public record ContactUpdatedEvent(
        String targetUsername, // Username to notify
        ContactNotificationDto notification) {
}
