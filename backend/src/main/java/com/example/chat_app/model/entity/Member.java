package com.example.chat_app.model.entity;

import com.example.chat_app.enums.MemberRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "members", uniqueConstraints = {
                @UniqueConstraint(columnNames = { "chat_id", "user_id" })
}, indexes = {
                @Index(name = "idx_members_chat", columnList = "chat_id"),
                @Index(name = "idx_members_user", columnList = "user_id")
})
public class Member {
        @Id
        @GeneratedValue(generator = "UUID")
        private UUID id;

        @JsonIgnore
        @ToString.Exclude
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "chat_id", nullable = false)
        private Chat chat;

        @JsonIgnore
        @ToString.Exclude
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Enumerated(EnumType.STRING)
        private MemberRole role;

        @CreationTimestamp
        @Column(name = "joined_at", updatable = false, nullable = false)
        private LocalDateTime joinedAt;
}
