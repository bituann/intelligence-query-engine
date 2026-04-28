package com.bituan.intelligence_query_engine.model.response;

import com.bituan.intelligence_query_engine.model.Profile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ProfileResponse {
    private String status;
    private String message;
    private Profile data;
}
