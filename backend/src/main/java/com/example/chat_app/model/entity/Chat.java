package com.example.chat_app.model.entity;

import com.example.chat_app.enums.ChatType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat")
@Entity
public class Chat {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ChatType chatType;

    @Column(name = "group_name", length = 100, nullable = true)
    private String groupName;

    @Column(name = "group_image", nullable = true)
    private String groupImage;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at", updatable = false, nullable = true)
    private LocalDateTime deletedAt;

    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "conversation", fetch = FetchType.LAZY)
    private List<Call_log> callLogs;
}
