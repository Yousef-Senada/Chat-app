package com.example.chat_app.factory;

import com.example.chat_app.exceptions.AppException;
import com.example.chat_app.model.dto.message.SendMessageRequest;
import com.example.chat_app.model.entity.Message;
import org.springframework.http.HttpStatus;

public class TextMessageProcessor implements MessageProcessor {
    @Override
    public void process(SendMessageRequest request, Message messageEntity) {
        if (request.content() == null || request.content().isBlank()) {
            throw new AppException("Text content cannot be empty", HttpStatus.BAD_REQUEST);
        }
        messageEntity.setContent(request.content());
    }
}
