package com.bituan.intelligence_query_engine.service.token;

import com.bituan.intelligence_query_engine.model.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface TokenService {
    String generateJwtToken(User user);
    String generateRefreshToken (User userId);
    boolean validateJwt(String token);
    boolean validateRefreshToken(String token);
    UserDetails loadUserDetailsFromJwt(String token);
}
