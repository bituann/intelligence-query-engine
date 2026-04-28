package com.bituan.intelligence_query_engine.model.response;

import com.bituan.intelligence_query_engine.model.entity.Profile;
import com.fasterxml.jackson.annotation.JsonInclude;
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
    private int totalPages;
    private Links links;
    private List<Profile> data;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class Links {
        private String self;
        private String next;
        private String prev;
    }
}

