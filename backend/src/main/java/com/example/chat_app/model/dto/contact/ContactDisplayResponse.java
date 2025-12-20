package com.example.chat_app.model.dto.contact;

import java.util.UUID;

public record ContactDisplayResponse(
        UUID ID,
        UUID contactUserId,
        String displayName,
        String contactUsername,
        String contactPhoneNumber
) {}
