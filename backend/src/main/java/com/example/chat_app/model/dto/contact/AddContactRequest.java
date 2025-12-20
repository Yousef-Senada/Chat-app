package com.example.chat_app.model.dto.contact;

import jakarta.validation.constraints.NotBlank;

public record AddContactRequest(
                @NotBlank(message = "Target phone number is required") String targetPhoneNumber,
                String customDisplayName) {
}
