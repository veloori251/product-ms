package com.example.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        Object metadata,
        String message,
        int status,
        LocalDateTime timestamp
){}
