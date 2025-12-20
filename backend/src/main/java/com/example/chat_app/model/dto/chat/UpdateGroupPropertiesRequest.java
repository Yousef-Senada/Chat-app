package com.example.chat_app.model.dto.chat;

import java.util.UUID;

public record UpdateGroupPropertiesRequest(
    UUID chatId,
    String newGroupName,
    String newGroupImageUrl
) {}
