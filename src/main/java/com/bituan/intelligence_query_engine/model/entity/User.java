package com.bituan.intelligence_query_engine.model.entity;

import com.bituan.intelligence_query_engine.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.ZonedDateTime;
import java.util.UUID;

@Table(name = "users")
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    @UuidGenerator(algorithm = org.hibernate.id.uuid.UuidVersion7Strategy.class)
    private UUID id;

    @Column(unique = true)
    private String githubId;

    private String username;
    private String email;
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @JsonProperty("is_active")
    private boolean isActive;
    private ZonedDateTime lastLoginAt;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;
}
