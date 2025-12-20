package com.example.chat_app.model.dto.chat;

import com.example.chat_app.model.dto.member.MemberDisplayDto;

import java.util.List;
import java.util.UUID;

public record ChatDisplayDto(
    UUID chatId,
    String chatType,          
    String groupName,         
    String groupImage,        
    List<MemberDisplayDto> members
) {}