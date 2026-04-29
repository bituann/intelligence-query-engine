package com.bituan.intelligence_query_engine.repository;

import com.bituan.intelligence_query_engine.model.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    @Transactional
    void deleteByOwnerId(UUID id);
    boolean existsByOwnerId(UUID id);
}
