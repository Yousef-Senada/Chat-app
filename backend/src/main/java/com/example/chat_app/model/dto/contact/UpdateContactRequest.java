package com.example.chat_app.model.dto.contact;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateContactRequest(
        @NotNull(message = "Target user ID is required") UUID targetUserId,
        String newDisplayName,
        String newPhoneNumber) {
}
