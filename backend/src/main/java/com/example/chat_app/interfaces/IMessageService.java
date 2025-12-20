package com.example.chat_app.interfaces;

import com.example.chat_app.model.dto.message.MessageDisplayDto;
import com.example.chat_app.model.dto.message.SendMessageRequest;
import com.example.chat_app.model.dto.message.UpdateMessageRequest;
import com.example.chat_app.model.entity.Message;
import com.example.chat_app.model.entity.User;
import org.springframework.data.domain.Page;

import java.util.UUID;

/**
 * Interface for message operations.
 */
public interface IMessageService {

    /**
     * Send a message to a chat.
     */
    void sendMessage(User sender, SendMessageRequest request);

    /**
     * Get paginated messages for a chat.
     */
    Page<MessageDisplayDto> getMessages(UUID chatId, User requester, int page, int size);

    /**
     * Edit a message content.
     */
    Message editMessage(User editor, UpdateMessageRequest request);

    /**
     * Delete (soft) a message.
     */
    void deleteMessage(User deleter, UUID messageId);
}
