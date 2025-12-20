package com.example.chat_app.model.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record CreateChatRequest(
        @NotBlank(message = "Chat type is required") String chatType,
        String groupName,
        String groupImage,
        @NotEmpty(message = "At least one member is required") List<UUID> membersId) {
}
