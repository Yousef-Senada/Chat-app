package com.example.chat_app.model.dto.message;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageDisplayDto(
        UUID messageId,
        SenderDto sender,
        String messageType,
        String content,
        String mediaUrl,
        LocalDateTime timestamp,
        boolean isEdited,
        boolean isDeleted
) {
    public static class Builder {
        private UUID messageId;
        private SenderDto sender;
        private String messageType;
        private String content;
        private String mediaUrl;
        private LocalDateTime timestamp;
        private boolean isEdited;
        private boolean isDeleted;

        public Builder messageId(UUID messageId) { this.messageId = messageId; return this; }
        public Builder sender(SenderDto sender) { this.sender = sender; return this; }
        public Builder messageType(String messageType) { this.messageType = messageType; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder mediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; return this; }
        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder isEdited(boolean isEdited) { this.isEdited = isEdited; return this; }
        public Builder isDeleted(boolean isDeleted) { this.isDeleted = isDeleted; return this; }

        public MessageDisplayDto build() {
            return new MessageDisplayDto(messageId, sender, messageType, content, mediaUrl, timestamp, isEdited, isDeleted);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
