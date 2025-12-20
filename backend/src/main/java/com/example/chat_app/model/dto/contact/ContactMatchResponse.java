package com.example.chat_app.model.dto.contact;


import java.util.UUID;

public record ContactMatchResponse(
        UUID id,
        String userName,
        String name,
        String phoneNumber
) {}
