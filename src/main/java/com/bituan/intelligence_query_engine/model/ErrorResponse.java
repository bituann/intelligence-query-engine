package com.bituan.intelligence_query_engine.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponse {
    private String status;
    private String message;
}
