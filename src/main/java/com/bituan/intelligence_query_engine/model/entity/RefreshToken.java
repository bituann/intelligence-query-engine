package com.bituan.intelligence_query_engine.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Table(name = "refresh_tokens")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne
    @JoinColumn(name = "owner_id", columnDefinition = "UUID", nullable = false)
    private User owner;

    private String tokenHash;
    private Instant createdAt;
    private Instant expiresAt;
}
