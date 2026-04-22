package com.bituan.intelligence_query_engine.component;
import com.bituan.intelligence_query_engine.exception.ServerException;
import com.bituan.intelligence_query_engine.model.Profile;
import com.bituan.intelligence_query_engine.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    @Value("${SEED_DATA}")
    private String encodedJson;

    private final ObjectMapper objectMapper;
    private final ProfileRepository profileRepository;

    @Override
    public void run(String... args) {
        if (profileRepository.count() != 0) {
            return;
        }

        if (encodedJson != null && !encodedJson.isBlank()) {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedJson);

            String json = new String(decodedBytes);

            try {
                List<Profile> users = objectMapper.readValue(json, new TypeReference<>(){});
                profileRepository.saveAll(users);
            } catch (Exception e) {
                throw new ServerException("Error initializing database");
            }
        }
    }
}

