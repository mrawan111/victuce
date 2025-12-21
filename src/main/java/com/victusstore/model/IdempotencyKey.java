package com.victusstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_idempotency_key_user_endpoint", 
            columnNames = {"key", "user_email", "endpoint"})
    },
    indexes = {
        @Index(name = "idx_idempotency_key", columnList = "key"),
        @Index(name = "idx_idempotency_user_email", columnList = "user_email"),
        @Index(name = "idx_idempotency_expires_at", columnList = "expires_at")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String key;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(nullable = false, length = 255)
    private String endpoint;

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

