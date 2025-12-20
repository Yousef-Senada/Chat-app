package com.example.chat_app.repository;

import com.example.chat_app.model.entity.Member;
import com.example.chat_app.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

        List<Member> findAllByUser(User user);

        List<Member> findAllByChatId(UUID chatId);

        Optional<Member> findByChatIdAndUserId(UUID chatId, UUID userId);

        long countByChatId(UUID chatId);

        /**
         * Fetch members by chat and user IDs with user data pre-loaded.
         * DISTINCT prevents duplicate rows from JOIN.
         */
        @Query("SELECT DISTINCT m FROM Member m " +
                        "JOIN FETCH m.user " +
                        "WHERE m.chat.id = :chatId AND m.user.id IN :userIds")
        List<Member> findByChatIdAndUserIdIn(@Param("chatId") UUID chatId, @Param("userIds") List<UUID> userIds);

        /**
         * Fetch all memberships for a user with their associated chats.
         * DISTINCT prevents duplicate rows from JOIN FETCH.
         */
        @Query("SELECT DISTINCT m FROM Member m " +
                        "JOIN FETCH m.chat c " +
                        "JOIN FETCH m.user u " +
                        "WHERE m.user = :user AND c.deletedAt IS NULL")
        List<Member> findAllByUserWithChat(@Param("user") User user);

        /**
         * Fetch all members of a chat with their user data.
         * DISTINCT prevents duplicate rows from JOIN FETCH.
         */
        @Query("SELECT DISTINCT m FROM Member m " +
                        "JOIN FETCH m.user " +
                        "WHERE m.chat.id = :chatId")
        List<Member> findAllByChatIdWithUser(@Param("chatId") UUID chatId);

        /**
         * Fetch all members for multiple chats at once (batch loading).
         * DISTINCT prevents duplicate rows from multiple JOINs.
         */
        @Query("SELECT DISTINCT m FROM Member m " +
                        "JOIN FETCH m.user " +
                        "JOIN FETCH m.chat " +
                        "WHERE m.chat.id IN :chatIds")
        List<Member> findAllByChatIdsWithUser(@Param("chatIds") List<UUID> chatIds);
}
