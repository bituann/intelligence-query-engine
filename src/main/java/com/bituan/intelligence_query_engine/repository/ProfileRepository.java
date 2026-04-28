package com.bituan.intelligence_query_engine.repository;

import com.bituan.intelligence_query_engine.model.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID>, JpaSpecificationExecutor<Profile> {
    boolean existsByName (String name);
    Optional<Profile> findByName(String name);
}
