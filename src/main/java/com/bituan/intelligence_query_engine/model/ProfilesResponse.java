package com.bituan.intelligence_query_engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class ProfilesResponse {
    private String status;
    private int page;
    private int limit;
    private int total;
    private List<Profile> data;
}
