package com.example.product.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Response<T>(
        T content,
        Object metadata
) {
}
