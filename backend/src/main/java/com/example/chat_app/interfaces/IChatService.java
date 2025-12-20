package com.example.chat_app.interfaces;

import com.example.chat_app.model.dto.chat.ChatDisplayDto;
import com.example.chat_app.model.dto.chat.CreateChatRequest;
import com.example.chat_app.model.dto.chat.UpdateGroupPropertiesRequest;
import com.example.chat_app.model.dto.member.MemberDisplayDto;
import com.example.chat_app.model.dto.member.UpdateMemberRoleRequest;
import com.example.chat_app.model.dto.member.UpdateMembershipRequest;
import com.example.chat_app.model.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * Interface for chat management operations.
 */
public interface IChatService {

    /**
     * Create a new chat (P2P or group).
     */
    ChatDisplayDto createChat(User owner, CreateChatRequest request);

    /**
     * Get all chats for a user.
     */
    List<ChatDisplayDto> getUserChats(User owner);

    /**
     * Get all members of a chat.
     */
    List<MemberDisplayDto> getChatMembers(UUID chatId, User requester);

    /**
     * Add members to a chat.
     */
    ChatDisplayDto addMember(User owner, UpdateMembershipRequest request);

    /**
     * Update group properties (name, image).
     */
    ChatDisplayDto updateGroupProperties(User owner, UpdateGroupPropertiesRequest request);

    /**
     * Update a member's role in a chat.
     */
    MemberDisplayDto updateMemberRole(User owner, UpdateMemberRoleRequest request);

    /**
     * Remove members from a chat.
     */
    void deleteMember(User owner, UpdateMembershipRequest request);
}
