package com.bituan.intelligence_query_engine.service.auth;

import com.bituan.intelligence_query_engine.model.response.AuthResponse;
import com.bituan.intelligence_query_engine.model.response.UserResponse;

public interface AuthService {
    String initializeGitHubOAuth(String state, String uri, String challenge, String method);
    AuthResponse signIn(String code, String verifier);
    AuthResponse refresh(String refreshToken);
    UserResponse getUser(String id);
    void logout(String id);
}
