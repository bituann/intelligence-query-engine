package com.bituan.intelligence_query_engine.component;
import com.bituan.intelligence_query_engine.exception.ServerException;
import com.bituan.intelligence_query_engine.model.Profile;
import com.bituan.intelligence_query_engine.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    @Value("${SEED_DATA_URL}")
    private String url;

    private final ObjectMapper objectMapper;
    private final ProfileRepository profileRepository;

    @Override
    public void run(String... args) {
        try {
            if (profileRepository.count() != 0) {
                return;
            }

            byte[] jsonBytes = new RestTemplate().getForObject(url, byte[].class);

            if (jsonBytes == null) {
                throw new Exception();
            }

            JsonNode json = objectMapper.readTree(new String(jsonBytes)).get("profiles");

            if (json == null || !json.isArray()) {
                throw new Exception("Invalid JSON structure: 'profiles' field missing or not an array");
            }

            List<Profile> users = objectMapper.convertValue(json, new TypeReference<>(){});
            profileRepository.saveAll(users);

        } catch (Exception e) {
            throw new ServerException(e.getMessage());
        }
    }
}

