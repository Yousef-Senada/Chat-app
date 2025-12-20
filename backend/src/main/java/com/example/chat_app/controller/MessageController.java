package com.example.chat_app.controller;

import com.example.chat_app.model.dto.message.UpdateMessageRequest;
import com.example.chat_app.model.dto.message.MessageDisplayDto;
import com.example.chat_app.model.dto.message.SendMessageRequest;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<?> sendMessage(@AuthenticationPrincipal User sender, @RequestBody SendMessageRequest request) {
        messageService.sendMessage(sender, request);
        return ResponseEntity.ok("Message sent successfully.");
    }


    @GetMapping("/{chatId}")
    public ResponseEntity<Page<MessageDisplayDto>> getChatMessages(
            @AuthenticationPrincipal User requester,
            @PathVariable UUID chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<MessageDisplayDto> messages = messageService.getMessages(chatId, requester, page, size);
        return ResponseEntity.ok(messages);
    }


    @PatchMapping
    public ResponseEntity<?> editMessage(@AuthenticationPrincipal User editor, @RequestBody UpdateMessageRequest request) {
        messageService.editMessage(editor, request);
        return ResponseEntity.ok("Message updated successfully.");
    }


    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(@AuthenticationPrincipal User deleter, @PathVariable UUID messageId) {
        messageService.deleteMessage(deleter, messageId);
        return ResponseEntity.noContent().build();
    }
}