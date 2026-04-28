package com.bituan.intelligence_query_engine.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GenderizeApiResponse {
    private String gender;
    private float probability;
    private int count;
}
