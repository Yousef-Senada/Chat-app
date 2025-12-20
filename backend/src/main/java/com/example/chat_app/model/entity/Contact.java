package com.example.chat_app.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "contacts", uniqueConstraints = {
                @UniqueConstraint(columnNames = { "owner_user_id", "contact_user_id" })
}, indexes = {
                @Index(name = "idx_contacts_owner", columnList = "owner_user_id")
})
public class Contact {
        @Id
        @GeneratedValue(generator = "UUID")
        private UUID id;

        @JsonIgnore
        @ToString.Exclude
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "owner_user_id", nullable = false)
        private User owner;

        @JsonIgnore
        @ToString.Exclude
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "contact_user_id", nullable = false)
        private User contactUser;

        @Column(name = "saved_name", length = 100)
        private String displayName;
}
