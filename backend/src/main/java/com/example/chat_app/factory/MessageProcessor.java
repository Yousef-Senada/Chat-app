package com.example.chat_app.factory;

import com.example.chat_app.model.dto.message.SendMessageRequest;
import com.example.chat_app.model.entity.Message;

public interface MessageProcessor {
    void process(SendMessageRequest request, Message messageEntity);
}
