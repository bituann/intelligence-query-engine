package com.bituan.intelligence_query_engine.model.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AuthResponse {
    private String status;
    private String accessToken;
    private String refreshToken;
}
