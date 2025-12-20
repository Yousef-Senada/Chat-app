package com.example.chat_app.events;

import com.example.chat_app.model.dto.member.MemberUpdateDto;
import java.util.UUID;

/**
 * Event published when chat members are updated (added/removed/role changed).
 */
public record MemberUpdatedEvent(
        UUID chatId,
        MemberUpdateDto update) {
}
