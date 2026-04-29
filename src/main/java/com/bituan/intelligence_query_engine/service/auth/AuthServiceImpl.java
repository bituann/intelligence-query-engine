package com.bituan.intelligence_query_engine.service.auth;

import com.bituan.intelligence_query_engine.enums.UserRole;
import com.bituan.intelligence_query_engine.exception.BadRequest;
import com.bituan.intelligence_query_engine.exception.NotFound;
import com.bituan.intelligence_query_engine.model.entity.RefreshToken;
import com.bituan.intelligence_query_engine.model.entity.User;
import com.bituan.intelligence_query_engine.model.response.AuthResponse;
import com.bituan.intelligence_query_engine.model.response.UserResponse;
import com.bituan.intelligence_query_engine.repository.RefreshTokenRepository;
import com.bituan.intelligence_query_engine.repository.UserRepository;
import com.bituan.intelligence_query_engine.service.token.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;

    private final TokenService tokenService;

    private final PasswordEncoder passwordEncoder;

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final WebClient webClient = WebClient.create();

    @Override
    public String initializeGitHubOAuth() {
        // generate a unique uuid and save in user session
        // this will serve as the state which you will pass as a query param

        ClientRegistration githubClientRegistration = clientRegistrationRepository.findByRegistrationId("github");

        return UriComponentsBuilder
                .fromUriString(githubClientRegistration.getProviderDetails().getAuthorizationUri())
                .queryParam("client_id", githubClientRegistration.getClientId())
                .queryParam("redirect_uri", githubClientRegistration.getRedirectUri())
                .queryParam("request_type", "code")
                .queryParam("access-type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();
    }

    @Override
    public AuthResponse signIn(String code, String verifier) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId("github");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", registration.getClientId());
        formData.add("client_secret", registration.getClientSecret());
        formData.add("redirect_uri", registration.getRedirectUri());

        if (!(verifier == null || verifier.isBlank())) {
            formData.add("code_verifier", verifier);
        }

        // Exchange code for tokens
        Map<String, String> tokenResponse = webClient.post()
                .uri(registration.getProviderDetails().getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters
                        .fromFormData(formData)
                )
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();

        if (tokenResponse == null) {
            throw new BadRequest("The provided code may be invalid");
        }

        String accessToken = tokenResponse.get("access_token");

        User user = getGitHubUser(registration.getProviderDetails().getUserInfoEndpoint().getUri(), accessToken);

        // don't forget to handle last login

        if (!userRepository.existsByGithubId(user.getGithubId())) {
            user = userRepository.save(user);
        }

        String jwt = tokenService.generateJwtToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        return AuthResponse.builder()
                .status("success")
                .accessToken(jwt)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        Optional<RefreshToken> matchedToken = tokenRepository.findAll().stream()
                .filter(t -> passwordEncoder.matches(refreshToken, t.getTokenHash()))
                .findFirst();

        if (matchedToken.isEmpty()) {
            throw new BadRequest("Invalid refresh token");
        }

        User user = userRepository.findById(matchedToken.get().getOwner().getId())
                .orElseThrow(() -> new BadRequest("Invalid refresh token"));

        String jwt = tokenService.generateJwtToken(user);
        String newRefreshToken = tokenService.generateRefreshToken(user);

        return AuthResponse.builder()
                .status("success")
                .accessToken(jwt)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public UserResponse getUser(String id) {
        User user = userRepository.findById(UUID.fromString(id)).orElseThrow(() -> new NotFound("User not found"));

        return UserResponse.builder()
                .status("success")
                .data(user)
                .build();
    }


    private User getGitHubUser (String uri, String accessToken) {
        Map<String, String> response = webClient.get()
                .uri(uri)
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(HttpHeaders.USER_AGENT, "Spring-Boot-App");
                })
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();

        return userRepository
                .findByGithubId(response.get("id"))
                .orElse(
                    User.builder()
                        .githubId(response.get("id"))
                        .username(response.get("login"))
                        .email(response.get("email"))
                        .avatarUrl(response.get("avatar_url"))
                        .role(UserRole.ANALYST)
                        .isActive(true)
                        .lastLoginAt(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS))
                        .build()
                );
    }
}
