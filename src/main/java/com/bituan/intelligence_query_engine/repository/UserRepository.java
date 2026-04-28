package com.bituan.intelligence_query_engine.repository;

import com.bituan.intelligence_query_engine.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByGithubId(String id);
    boolean existsByGithubId(String id);
}
