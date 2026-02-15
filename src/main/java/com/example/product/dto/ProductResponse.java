package com.example.product.dto;

import com.example.product.enums.ProductStatus;
import com.example.product.enums.StockStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
        String id,
        String SKU,
        String name,
        String description,
        Double price,
        Integer availableQuantity,
        StockStatus stockStatus,
        ProductStatus productStatus,
        String categoryId,
        LocalDateTime createdAt


)
{}
