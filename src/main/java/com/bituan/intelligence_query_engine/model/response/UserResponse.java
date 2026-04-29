package com.bituan.intelligence_query_engine.model.response;

import com.bituan.intelligence_query_engine.model.entity.User;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResponse {
    private String status;
    private User data;
}
