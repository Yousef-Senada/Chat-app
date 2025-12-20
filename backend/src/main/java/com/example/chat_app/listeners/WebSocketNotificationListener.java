package com.example.chat_app.listeners;

import com.example.chat_app.events.*;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Observer that listens to application events and sends WebSocket
 * notifications.
 * This centralizes all WebSocket notification logic in one place.
 */
@Component
public class WebSocketNotificationListener {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle message sent events - broadcast to chat topic
     */
    @EventListener
    public void onMessageSent(MessageSentEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/chat/" + event.chatId(),
                event.message());
    }

    /**
     * Handle new chat created - notify each member individually
     */
    @EventListener
    public void onChatCreated(ChatCreatedEvent event) {
        for (String username : event.usernames()) {
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/queue/newChat",
                    event.chat());
        }
    }

    /**
     * Handle member updates (added/removed/role changed) - broadcast to chat
     */
    @EventListener
    public void onMemberUpdated(MemberUpdatedEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/chat/" + event.chatId() + "/members",
                event.update());
    }

    /**
     * Handle chat removed - notify specific user
     */
    @EventListener
    public void onChatRemoved(ChatRemovedEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.username(),
                "/queue/chatRemoved",
                event.chatId());
    }

    /**
     * Handle chat property updates - broadcast to chat
     */
    @EventListener
    public void onChatUpdated(ChatUpdatedEvent event) {
        messagingTemplate.convertAndSend(
                "/topic/chat/" + event.chatId() + "/updates",
                event.chat());
    }

    /**
     * Handle contact updates - notify specific user
     */
    @EventListener
    public void onContactUpdated(ContactUpdatedEvent event) {
        messagingTemplate.convertAndSendToUser(
                event.targetUsername(),
                "/queue/contactUpdates",
                event.notification());
    }
}
