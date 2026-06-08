package com.example.template.integrations.gmail.entity;

import com.example.template.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "gmail_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GmailToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** AES-256-GCM encrypted. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String accessToken;

    /** AES-256-GCM encrypted. Null if the provider didn't issue one. */
    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    @Column(length = 50)
    private String tokenType;

    @Column(length = 500)
    private String scope;

    private Instant expiresAt;

    /** The Gmail address this token grants access to. */
    @Column(length = 255)
    private String gmailEmail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
