package com.example.chat_app.factory;

import com.example.chat_app.exceptions.AppException;
import com.example.chat_app.model.dto.message.SendMessageRequest;
import com.example.chat_app.model.entity.Message;
import org.springframework.http.HttpStatus;

public class ImageMessageProcessor implements MessageProcessor{
    @Override
    public void process(SendMessageRequest request, Message messageEntity) {
        if (request.mediaUrl() == null) {
            throw new AppException("Image URL is required", HttpStatus.BAD_REQUEST);
        }
        messageEntity.setMediaUrl(request.mediaUrl());
        messageEntity.setContent(request.content() != null ? request.content() : "📹 Photo");
    }
}
