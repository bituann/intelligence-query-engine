package com.bituan.intelligence_query_engine.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UnprocessableEntity extends RuntimeException {
    private String message;
}

