package com.bituan.intelligence_query_engine.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BadRequest extends RuntimeException {
    private String message;
}
