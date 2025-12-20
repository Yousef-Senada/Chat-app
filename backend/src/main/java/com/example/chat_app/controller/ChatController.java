package com.example.chat_app.controller;

import com.example.chat_app.model.dto.chat.ChatDisplayDto;
import com.example.chat_app.model.dto.chat.CreateChatRequest;
import com.example.chat_app.model.dto.chat.UpdateGroupPropertiesRequest;
import com.example.chat_app.model.dto.member.UpdateMemberRoleRequest;
import com.example.chat_app.model.dto.member.UpdateMembershipRequest;
import com.example.chat_app.model.dto.member.MemberDisplayDto;

import com.example.chat_app.model.entity.User;
import com.example.chat_app.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public ResponseEntity<List<ChatDisplayDto>> getUserChats(@AuthenticationPrincipal User owner) {
        List<ChatDisplayDto> chats = chatService.getUserChats(owner);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/members/{chatId}")
    public ResponseEntity<List<MemberDisplayDto>> getChatMembers(@AuthenticationPrincipal User owner,
            @PathVariable UUID chatId) {
        List<MemberDisplayDto> members = chatService.getChatMembers(chatId, owner);
        return ResponseEntity.ok(members);
    }

    @PostMapping
    public ResponseEntity<?> createChat(@AuthenticationPrincipal User owner, @RequestBody CreateChatRequest request) {
        chatService.createChat(owner, request);
        return ResponseEntity.ok("Chat created successfully.");
    }

    @PatchMapping("/properties")
    public ResponseEntity<?> updateGroupProperties(@AuthenticationPrincipal User owner,
            @RequestBody UpdateGroupPropertiesRequest request) {
        chatService.updateGroupProperties(owner, request);
        return ResponseEntity.ok("Group properties updated successfully.");
    }

    @PostMapping("/members")
    public ResponseEntity<?> addMembers(@AuthenticationPrincipal User owner,
            @RequestBody UpdateMembershipRequest request) {
        chatService.addMember(owner, request);
        return ResponseEntity.ok("Members added successfully.");
    }

    @PatchMapping("/roles")
    public ResponseEntity<?> updateMemberRole(@AuthenticationPrincipal User owner,
            @RequestBody UpdateMemberRoleRequest request) {
        chatService.updateMemberRole(owner, request);
        return ResponseEntity.ok("Member role updated successfully.");
    }

    @DeleteMapping("/members")
    public ResponseEntity<?> deleteMember(@AuthenticationPrincipal User owner,
            @RequestBody UpdateMembershipRequest request) {
        chatService.deleteMember(owner, request);
        return ResponseEntity.noContent().build();
    }

}
