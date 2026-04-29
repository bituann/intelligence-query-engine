package com.bituan.intelligence_query_engine.service.auth;

import com.bituan.intelligence_query_engine.model.response.AuthResponse;
import com.bituan.intelligence_query_engine.model.response.UserResponse;
import org.springframework.http.HttpHeaders;

public interface AuthService {
    String initializeGitHubOAuth();
    AuthResponse signIn(String code, String verifier);
    AuthResponse refresh(String refreshToken);
    UserResponse getUser(String id);
}
