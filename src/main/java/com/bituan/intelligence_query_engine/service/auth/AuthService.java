package com.bituan.intelligence_query_engine.service.auth;

import com.bituan.intelligence_query_engine.model.response.AuthResponse;

public interface AuthService {
    String initializeGitHubOAuth();
    AuthResponse signIn(String code, String verifier);
    AuthResponse refresh(String refreshToken);
}
