package com.example.chat_app.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.chat_app.events.*;
import com.example.chat_app.exceptions.AppException;

import com.example.chat_app.enums.ChatType;
import com.example.chat_app.enums.MemberRole;
import com.example.chat_app.model.dto.chat.ChatDisplayDto;
import com.example.chat_app.model.dto.chat.CreateChatRequest;
import com.example.chat_app.model.dto.chat.UpdateGroupPropertiesRequest;
import com.example.chat_app.model.dto.member.MemberDisplayDto;
import com.example.chat_app.model.dto.member.MemberUpdateDto;
import com.example.chat_app.model.dto.member.UpdateMemberRoleRequest;
import com.example.chat_app.model.dto.member.UpdateMembershipRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.chat_app.model.entity.Chat;
import com.example.chat_app.model.entity.Member;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.repository.ChatRepository;
import com.example.chat_app.repository.UserRepository;
import com.example.chat_app.repository.MemberRepository;
import com.example.chat_app.interfaces.IChatService;

@Service
public class ChatService implements IChatService {
    private final ChatRepository chatRepo;
    private final UserRepository userRepo;
    private final MemberRepository memberRepo;
    private final ApplicationEventPublisher eventPublisher;

    public ChatService(ChatRepository chatRepo, UserRepository userRepo, MemberRepository memberRepo,
            ApplicationEventPublisher eventPublisher) {
        this.chatRepo = chatRepo;
        this.userRepo = userRepo;
        this.memberRepo = memberRepo;
        this.eventPublisher = eventPublisher;
    }

    private MemberDisplayDto convertToMemberDisplayDto(Member member) {
        return new MemberDisplayDto(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getRole().name());
    }

    private ChatDisplayDto convertToDisplayDto(Chat chat, List<Member> members) {

        List<MemberDisplayDto> memberDtos = members.stream()
                .map(this::convertToMemberDisplayDto)
                .collect(Collectors.toList());

        return new ChatDisplayDto(
                chat.getId(),
                chat.getChatType().name(),
                chat.getGroupName(),
                chat.getGroupImage(),
                memberDtos);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userChats", key = "#owner.id")
    public ChatDisplayDto createChat(User owner, CreateChatRequest request) {

        List<User> usersToAdd = userRepo.findAllById(request.membersId());

        if (usersToAdd.size() != request.membersId().size()) {
            throw new AppException("Some users were not found", HttpStatus.BAD_REQUEST);
        }

        if (usersToAdd.stream().noneMatch(user -> user.getId().equals(owner.getId()))) {
            usersToAdd.add(owner);
        }

        if ("P2P".equals(request.chatType())) {
            if (usersToAdd.size() != 2) {
                throw new AppException("P2P chat must have exactly 2 users", HttpStatus.BAD_REQUEST);
            }
        } else if ("GROUP".equals(request.chatType())) {
            if (request.groupName() == null || request.groupName().trim().isEmpty()) {
                throw new AppException("Group name is required", HttpStatus.BAD_REQUEST);
            }

            if (usersToAdd.size() < 3) {
                throw new AppException("Group chat must have at least 3 users", HttpStatus.BAD_REQUEST);
            }
        } else {
            throw new AppException("Invalid chat type", HttpStatus.BAD_REQUEST);
        }

        Chat newChat = new Chat();
        ChatType type = ChatType.valueOf(request.chatType().toUpperCase());
        newChat.setChatType(type);
        newChat.setGroupName(request.groupName());
        newChat.setGroupImage(request.groupImage());
        Chat savedChat = chatRepo.save(newChat);

        List<Member> members = usersToAdd.stream()
                .map(user -> {
                    Member member = new Member();
                    member.setChat(savedChat);
                    member.setUser(user);

                    if (user.getId().equals(owner.getId())) {
                        member.setRole(MemberRole.ADMIN);
                    } else {
                        member.setRole(MemberRole.MEMBER);
                    }
                    return member;
                })
                .collect(Collectors.toList());

        memberRepo.saveAll(members);

        ChatDisplayDto chatDto = convertToDisplayDto(savedChat, members);

        // Publish event - Observer pattern
        List<String> usernames = usersToAdd.stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        eventPublisher.publishEvent(new ChatCreatedEvent(chatDto, usernames));

        return chatDto;
    }

    /**
     * Get all chats for a user - CACHED for 10 minutes.
     * Cache is invalidated when user creates/joins/leaves a chat.
     */
    @Override
    @Cacheable(value = "userChats", key = "#owner.id")
    public List<ChatDisplayDto> getUserChats(User owner) {
        List<Member> userMemberships = memberRepo.findAllByUserWithChat(owner);

        if (userMemberships.isEmpty()) {
            return List.of();
        }

        List<UUID> chatIds = userMemberships.stream()
                .map(m -> m.getChat().getId())
                .collect(Collectors.toList());

        List<Member> allMembers = memberRepo.findAllByChatIdsWithUser(chatIds);

        Map<UUID, List<Member>> membersByChat = allMembers.stream()
                .collect(Collectors.groupingBy(m -> m.getChat().getId()));

        return userMemberships.stream()
                .map(membership -> {
                    Chat chat = membership.getChat();
                    List<Member> chatMembers = membersByChat.getOrDefault(chat.getId(), List.of());
                    return convertToDisplayDto(chat, chatMembers);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all members of a chat - CACHED for 10 minutes.
     * Cache is invalidated when members are added/removed.
     */
    @Override
    @Cacheable(value = "chatMembers", key = "#chatId")
    public List<MemberDisplayDto> getChatMembers(UUID chatId, User requester) {

        boolean isMember = memberRepo.findByChatIdAndUserId(chatId, requester.getId()).isPresent();
        if (!isMember) {
            throw new AppException("User is not authorized to view the members of this chat.", HttpStatus.FORBIDDEN);
        }

        List<Member> members = memberRepo.findAllByChatIdWithUser(chatId);

        return members.stream()
                .map(this::convertToMemberDisplayDto)
                .collect(Collectors.toList());
    }

    public void authorizeGroupAdmin(UUID chatId, User user) {
        Member member = memberRepo.findByChatIdAndUserId(chatId, user.getId())
                .orElseThrow(() -> new AppException("User is not a member of the chat", HttpStatus.FORBIDDEN));

        MemberRole role = member.getRole();

        if (!MemberRole.ADMIN.equals(role)) {
            throw new AppException("User does not have administrative privileges for this group.",
                    HttpStatus.FORBIDDEN);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "chatMembers", key = "#request.chatId()")
    public ChatDisplayDto addMember(User owner, UpdateMembershipRequest request) {
        authorizeGroupAdmin(request.chatId(), owner);

        Chat chat = chatRepo.findById(request.chatId())
                .orElseThrow(() -> new AppException("chat not found", HttpStatus.NOT_FOUND));

        List<User> usersToAdd = userRepo.findAllById(request.memberUserIds());

        if (usersToAdd.size() != request.memberUserIds().size()) {
            throw new AppException("One or more user IDs to add are invalid.", HttpStatus.BAD_REQUEST);
        }

        List<UUID> existingMembersIds = memberRepo.findAllByChatId(request.chatId())
                .stream()
                .map(member -> member.getUser().getId())
                .collect(Collectors.toList());

        List<Member> newMembers = usersToAdd.stream()
                .filter(user -> !existingMembersIds.contains(user.getId()))
                .map(user -> {
                    Member member = new Member();
                    member.setChat(chat);
                    member.setUser(user);
                    member.setRole(MemberRole.MEMBER);
                    return member;
                })
                .collect(Collectors.toList());

        memberRepo.saveAll(newMembers);

        List<MemberDisplayDto> addedMembersDto = newMembers.stream()
                .map(this::convertToMemberDisplayDto)
                .collect(Collectors.toList());

        MemberUpdateDto updateDto = new MemberUpdateDto(
                request.chatId(),
                addedMembersDto,
                "MEMBER_ADDED");

        // Publish event - Observer pattern
        eventPublisher.publishEvent(new MemberUpdatedEvent(chat.getId(), updateDto));

        List<Member> allMembers = memberRepo.findAllByChatId(chat.getId());
        return convertToDisplayDto(chat, allMembers);
    }

    @Override
    @Transactional
    public ChatDisplayDto updateGroupProperties(User owner, UpdateGroupPropertiesRequest request) {

        authorizeGroupAdmin(request.chatId(), owner);

        Chat chat = chatRepo.findById(request.chatId())
                .orElseThrow(() -> new AppException("Chat not found", HttpStatus.NOT_FOUND));

        if (request.newGroupName() != null && !request.newGroupName().trim().isEmpty()) {
            chat.setGroupName(request.newGroupName());
        }

        if (request.newGroupImageUrl() != null) {
            chat.setGroupImage(request.newGroupImageUrl());
        }

        Chat savedChat = chatRepo.save(chat);

        List<Member> members = memberRepo.findAllByChatId(savedChat.getId());
        ChatDisplayDto chatDto = convertToDisplayDto(savedChat, members);

        // Publish event - Observer pattern
        eventPublisher.publishEvent(new ChatUpdatedEvent(savedChat.getId(), chatDto));

        return chatDto;
    }

    @Override
    @Transactional
    public MemberDisplayDto updateMemberRole(User owner, UpdateMemberRoleRequest request) {

        authorizeGroupAdmin(request.chatId(), owner);

        Member targetMember = memberRepo.findByChatIdAndUserId(request.chatId(), request.targetUserId())
                .orElseThrow(() -> new AppException("Target user is not a member of this chat.", HttpStatus.FORBIDDEN));

        if (MemberRole.ADMIN.equals(targetMember.getRole()) && !owner.getId().equals(targetMember.getUser().getId())) {
            throw new AppException("Cannot modify the role of an existing group ADMIN.", HttpStatus.FORBIDDEN);
        }

        String newRoleUpper = request.newRole().toUpperCase();

        if (!"ADMIN".equalsIgnoreCase(newRoleUpper) && !"MEMBER".equalsIgnoreCase(newRoleUpper)) {
            throw new AppException("Invalid role provided. Role must be ADMIN or MEMBER.", HttpStatus.BAD_REQUEST);
        }

        MemberRole newRoleEnum = MemberRole.valueOf(newRoleUpper);
        targetMember.setRole(newRoleEnum);

        Member updatedMember = memberRepo.save(targetMember);

        MemberDisplayDto memberDto = convertToMemberDisplayDto(updatedMember);

        MemberUpdateDto updateDto = new MemberUpdateDto(
                request.chatId(),
                List.of(memberDto),
                "ROLE_UPDATED");

        // Publish event - Observer pattern
        eventPublisher.publishEvent(new MemberUpdatedEvent(request.chatId(), updateDto));

        return memberDto;

    }

    /**
     * OPTIMIZED: Delete members from a chat.
     * 
     * Before: N queries in loop for admin check + M queries for user lookups
     * After: 1 query to get members (with user data pre-loaded)
     */
    @Override
    @CacheEvict(value = "chatMembers", key = "#request.chatId()")
    public void deleteMember(User owner, UpdateMembershipRequest request) {
        // Use optimized query that fetches user data along with members
        List<Member> membersToRemove = memberRepo.findByChatIdAndUserIdIn(request.chatId(), request.memberUserIds());

        if (membersToRemove.isEmpty()) {
            throw new AppException("No matching members found to remove from the chat.", HttpStatus.NOT_FOUND);
        }

        // Check owner's admin status ONCE before the loop (not inside!)
        boolean isAdmin = memberRepo.findByChatIdAndUserId(request.chatId(), owner.getId())
                .map(m -> MemberRole.ADMIN.equals(m.getRole()))
                .orElse(false);

        // Validate all members can be removed (no DB queries in this loop now!)
        for (Member targetMember : membersToRemove) {
            // Prevent admin from removing themselves
            if (targetMember.getUser().getId().equals(owner.getId())
                    && MemberRole.ADMIN.equals(targetMember.getRole())) {
                throw new AppException("Group owner cannot remove themselves using this function.",
                        HttpStatus.FORBIDDEN);
            }

            // Check permission: must be admin OR removing yourself
            boolean isRemovingSelf = targetMember.getUser().getId().equals(owner.getId());

            if (!isAdmin && !isRemovingSelf) {
                throw new AppException("User does not have permission to remove one of the specified members.",
                        HttpStatus.FORBIDDEN);
            }
        }

        // Delete all members
        memberRepo.deleteAll(membersToRemove);

        // Publish events - Observer pattern
        membersToRemove.forEach(member -> {
            String username = member.getUser().getUsername();
            eventPublisher.publishEvent(new ChatRemovedEvent(request.chatId(), username));
        });

        MemberUpdateDto updateDto = new MemberUpdateDto(
                request.chatId(),
                null,
                "MEMBER_REMOVED");

        eventPublisher.publishEvent(new MemberUpdatedEvent(request.chatId(), updateDto));
    }
}
