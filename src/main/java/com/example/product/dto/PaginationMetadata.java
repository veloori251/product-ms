package com.example.product.dto;

public record PaginationMetadata (
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
){
}
