package com.bituan.intelligence_query_engine.service.token;

import com.bituan.intelligence_query_engine.model.entity.CustomUserDetails;
import com.bituan.intelligence_query_engine.model.entity.RefreshToken;
import com.bituan.intelligence_query_engine.model.entity.User;
import com.bituan.intelligence_query_engine.repository.RefreshTokenRepository;
import com.bituan.intelligence_query_engine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final RefreshTokenRepository tokenRepository;

    @Override
    public String generateJwtToken(User user) {
        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .subject(user.getId().toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(3, ChronoUnit.MINUTES))
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    @Override
    public String generateRefreshToken(User user) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);

        String token = "ISLB_refresh_" + Base64.getEncoder().withoutPadding().encodeToString(randomBytes);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(passwordEncoder.encode(token))
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plus(5, ChronoUnit.MINUTES))
                .owner(user)
                .build();

        tokenRepository.deleteByOwnerId(user.getId());
        tokenRepository.save(refreshToken);

        return token;
    }

    @Override
    public boolean validateJwt(String token) {
        Jwt jwt = decodeJwt(token);

        return jwt != null &&
                jwt.getExpiresAt() != null &&
                jwt.getExpiresAt().isAfter(Instant.now()) &&
                userRepository.existsById(UUID.fromString(jwt.getSubject()));

    }

    @Override
    public boolean validateRefreshToken(String token) {

        Optional<RefreshToken> refreshToken = tokenRepository.findAll().stream().filter(dbToken ->
                passwordEncoder.matches(token, dbToken.getTokenHash())).findFirst();

        return refreshToken.isPresent() && !refreshToken.get().getExpiresAt().isAfter(Instant.now());
    }

    @Override
    public UserDetails loadUserDetailsFromJwt(String token) {
        Jwt jwt = decodeJwt(token);
        UUID userId = UUID.fromString(jwt.getSubject());

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return null;
        }

        return new CustomUserDetails(user);
    }

    private Jwt decodeJwt (String token) {
        return jwtDecoder.decode(token);
    }
}
