package com.example.chat_app.service;

import com.example.chat_app.events.MessageSentEvent;
import com.example.chat_app.exceptions.AppException;
import com.example.chat_app.enums.MemberRole;
import com.example.chat_app.enums.MessageType;
import com.example.chat_app.factory.MessageProcessor;
import com.example.chat_app.factory.MessageProcessorFactory;
import com.example.chat_app.model.dto.message.MessageDisplayDto;
import com.example.chat_app.model.dto.message.SendMessageRequest;
import com.example.chat_app.model.dto.message.SenderDto;
import com.example.chat_app.model.dto.message.UpdateMessageRequest;
import com.example.chat_app.model.entity.Chat;
import com.example.chat_app.model.entity.Message;
import com.example.chat_app.model.entity.User;
import com.example.chat_app.repository.ChatRepository;
import com.example.chat_app.repository.MemberRepository;
import com.example.chat_app.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import com.example.chat_app.interfaces.IMessageService;

@Service
public class MessageService implements IMessageService {

    private final MessageRepository messageRepo;
    private final ChatRepository chatRepo;
    private final MemberRepository memberRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageProcessorFactory messageFactory;

    public MessageService(MessageRepository messageRepo, ChatRepository chatRepo, MemberRepository memberRepo,
            ApplicationEventPublisher eventPublisher, MessageProcessorFactory messageFactory) {
        this.messageRepo = messageRepo;
        this.chatRepo = chatRepo;
        this.memberRepo = memberRepo;
        this.eventPublisher = eventPublisher;
        this.messageFactory = messageFactory;
    }

    private MessageDisplayDto convertToDisplayDto(Message message) {
        SenderDto senderDto = new SenderDto(message.getSender().getId(), message.getSender().getUsername());

        return MessageDisplayDto.builder()
                .messageId(message.getId())
                .sender(senderDto)
                .messageType(message.getType().name())
                .content(message.isDeleted() ? "Message has been deleted" : message.getContent())
                .mediaUrl(message.getMediaUrl())
                .timestamp(message.getSentAt())
                .isEdited(message.isEdited())
                .isDeleted(message.isDeleted())
                .build();
    }

    @Override
    @Transactional
    public void sendMessage(User sender, SendMessageRequest request) {
        Chat chat = chatRepo.findById(request.chatId())
                .orElseThrow(() -> new AppException("Chat not found.", HttpStatus.NOT_FOUND));

        // Verify sender is a member of the chat
        boolean isMember = memberRepo.findByChatIdAndUserId(chat.getId(), sender.getId()).isPresent();
        if (!isMember) {
            throw new AppException("User is not a member of this chat.", HttpStatus.FORBIDDEN);
        }

        Message message = new Message();
        message.setSender(sender);
        message.setChat(chat);
        message.setType(MessageType.valueOf(request.messageType().toUpperCase()));
        message.setSentAt(LocalDateTime.now());

        MessageProcessor processor = messageFactory.getProcessor(request.messageType());
        processor.process(request, message);

        Message savedMessage = messageRepo.save(message);

        // Publish event - Observer pattern
        MessageDisplayDto messageDto = convertToDisplayDto(savedMessage);
        eventPublisher.publishEvent(new MessageSentEvent(chat.getId(), messageDto));
    }

    @Override
    public Page<MessageDisplayDto> getMessages(UUID chatId, User requester, int page, int size) {

        boolean isMember = memberRepo.findByChatIdAndUserId(chatId, requester.getId()).isPresent();

        if (!isMember) {
            throw new AppException("User is not a member of this chat and cannot view messages.", HttpStatus.FORBIDDEN);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());

        Page<Message> messagePage = messageRepo.findAllByChatIdOrderBySentAtDesc(chatId, pageable);

        return messagePage.map(this::convertToDisplayDto);
    }

    @Override
    @Transactional
    public Message editMessage(User editor, UpdateMessageRequest request) {

        Message message = messageRepo.findById(request.messageId())
                .orElseThrow(() -> new AppException("Message not found.", HttpStatus.NOT_FOUND));

        if (!message.getSender().getId().equals(editor.getId())) {
            throw new AppException("User is not authorized to edit this message.", HttpStatus.FORBIDDEN);
        }

        if (!MessageType.TEXT.equals(message.getType())) {
            throw new AppException("Only text messages can be edited.", HttpStatus.BAD_REQUEST);
        }

        if (request.newContent() == null || request.newContent().trim().isEmpty()) {
            throw new AppException("New message content cannot be empty.", HttpStatus.BAD_REQUEST);
        }

        message.setContent(request.newContent());

        message.setEdited(true);

        Message updatedMessage = messageRepo.save(message);

        // Publish event - Observer pattern
        MessageDisplayDto messageDto = convertToDisplayDto(updatedMessage);
        eventPublisher.publishEvent(new MessageSentEvent(updatedMessage.getChat().getId(), messageDto));

        return updatedMessage;
    }

    @Override
    @Transactional
    public void deleteMessage(User deleter, UUID messageId) {
        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> new AppException("Message not found.", HttpStatus.NOT_FOUND));

        boolean isSender = message.getSender().getId().equals(deleter.getId());

        boolean isAdmin = memberRepo.findByChatIdAndUserId(message.getChat().getId(), deleter.getId())
                .map(member -> MemberRole.ADMIN.equals(member.getRole()))
                .orElse(false);

        if (!isSender && !isAdmin) {
            throw new AppException("User is not authorized to delete this message.", HttpStatus.FORBIDDEN);
        }

        message.setDeleted(true);

        Message savedMessage = messageRepo.save(message);

        // Publish event - Observer pattern
        MessageDisplayDto messageDto = convertToDisplayDto(savedMessage);
        eventPublisher.publishEvent(new MessageSentEvent(savedMessage.getChat().getId(), messageDto));
    }
}
